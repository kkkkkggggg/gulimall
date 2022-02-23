package com.atck.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atck.common.utils.PageUtils;
import com.atck.common.utils.Query;
import com.atck.gulimall.product.entity.CategoryBrandRelationEntity;
import com.atck.gulimall.product.service.CategoryBrandRelationService;
import com.atck.gulimall.product.vo.Catalog2Vo;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atck.gulimall.product.dao.CategoryDao;
import com.atck.gulimall.product.entity.CategoryEntity;
import com.atck.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree()
    {
        //1.查出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //2.组装成父子的树形结构
        //2-1:找到所有一级分类
        List<CategoryEntity> collect = entities.stream().filter((categoryEntity) -> categoryEntity.getParentCid() == 0)
                .map((menu) ->
                {
                    menu.setChildren(getChildrens(menu,entities));
                    return menu;
                })
                .sorted((menu1,menu2) -> (menu1.getSort() == null ? 0 :menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort()))
                .collect(Collectors.toList());
        return collect;
    }

    @Override
    public void removeMenuByIDs(List<Long> asList)
    {
        // TODO: 1.检查当前删除的菜单，是否被别的地方引用

        //
        baseMapper.deleteBatchIds(asList);
    }

    /**
     * 找到catelogId的完整路径
     *
     * @param catelogId
     * @return
     */
    @Override
    public Long[] findCatelogPath(Long catelogId)
    {
        List<Long> path = new ArrayList<>();

        List<Long> parentPath = getCatelogPath(catelogId, path);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[0]);
    }

    /**
     * 级联更新所有关联的表
     * @param category
     * @CacheEvict 缓存失效模式的使用
     * 1.@Caching 同时进行多种缓存操作
     * 2.@CacheEvict(value = "category",allEntries = true),指定删除某个分区下的所有数据
     * 3.存储同一类型的数据，都可以指定成同一个分区，分区名就是缓存前缀
     */
    // @CacheEvict(value = "category",key = "'getLevel1Categories'")

    // @Caching(evict = {
    //         @CacheEvict(value = "category",key = "'getLevel1Categories'"),
    //         @CacheEvict(value = "category",key = "'getCatalogJson'")
    // })

    // @CachePut //双写模式
    @CacheEvict(value = "category",allEntries = true)//失效模式
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category)
    {
        this.updateById(category);
        if (!StringUtils.isEmpty(category.getName()))
        {
            categoryBrandRelationService.updateCategory(category.getCatId(),category.getName());
        }

        //1.双写模式：同时更新缓存中的数据
        //2.redis.del("cataloJson");等待下次主动查询进行更新
    }

    @Override
    public List<Long> getCatelogPath(Long catelogId,List<Long> path)
    {
        path.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        Long parentCid = byId.getParentCid();
        if (!parentCid.equals(0L))
        {
            getCatelogPath(parentCid,path);
        }
        return path;
    }

    /**
     * 1.每一个需要缓存的数据我们都来指定要放到哪个名字的缓存【缓存的分区（按业务类型分）】
     * 2.@Cacheable("category")  代表当前方法的结果需要缓存，如果缓存中有，方法不用调用，如果缓存中没有，调用方法，最后将方法的结果放入缓存中
     * 3.默认行为
     *      1.缓存中没有，调用方法，最后将方法的结果放入缓存中
     *      2.key是自动生成的，缓存的名字：category::SimpleKey []
     *      3.缓存的value的值：默认使用jdk序列化机制，将序列化后的数据存到redis
     *      4.默认数据永不过期
     * 自定义操作：
     *      1.指定生成的缓存使用的key： key属性指定，接收一个SpEL表达式
     *          SpEL表达式的详细：https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache-declarative-xml
     *      2.指定数据的过期时间
     *      3.指定value保存的格式为json
     *              自定义RedisCacheConfiguration即可
     * 4.Spring-Cache的不足
     *      1.读模式
     *          缓存穿透：查询一个null数据，解决：缓存空数据：cache-null-values=true
     *          缓存穿透：大量并发进来同时查询一个正好过期的数据，解决：加锁？默认是不加锁的：sync=true 加锁（本地锁）
     *          缓存雪崩：大量key同时过期，解决，加随机时间，加上过期时间
     *      2.写模式（缓存与数据库一致）
     *          1.读写加锁（适合读多写少的数据）
     *          2.映入canal，感知到MySQL的更新就去更新缓存
     *          3.读多写多，直接去数据库查就行
     *      总结：常规数据（读多写少，即时性，一致性要求不高的数据，完全可以使用spring-cache）,写模式（只要缓存的数据有过期时间就足够了）
     *           特殊数据：特殊设计
     *   原理：
     *      CacheManager（RedisCacheManager）->Cache（RedisCache）->Cache负责缓存的读写
     *
     *
     * @return
     */

    @Cacheable(value = "category",key = "#root.method.name",sync = true)
    @Override
    public List<CategoryEntity> getLevel1Categories()
    {
        System.out.println("getLevel1Categories........");
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("cat_level", 1));
        return categoryEntities;
    }

    @Cacheable(value = "category",key = "#root.method.name")
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson()
    {
        /**
         * 1.将数据库的多次查询变为一次
         *
         */
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);


        //1.查出所有一级分类
        List<CategoryEntity> level1Categories = getParent_cid(categoryEntities, 0L);

        //2.封装数据
        Map<String, List<Catalog2Vo>> collect = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v ->
        {
            //1.遍历每一个一级分类，查到这个一级分类的所有二级分类
            List<CategoryEntity> entityList = getParent_cid(categoryEntities, v.getCatId());
            //2.封装上面的结果
            List<Catalog2Vo> catalog2Vos = null;
            if (entityList != null)
            {
                catalog2Vos = entityList.stream().map(level2 ->
                {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, level2.getCatId().toString(), level2.getName());
                    //1.找当前二级分类下的三级分类
                    List<CategoryEntity> categoryLevel3Entities = getParent_cid(categoryEntities, level2.getCatId());
                    List<Catalog2Vo.Catalog3Vo> catalog3Vos = null;
                    if (categoryLevel3Entities != null)
                    {
                        catalog3Vos = categoryLevel3Entities.stream().map(level3 ->
                        {
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(level2.getCatId().toString(), level3.getCatId().toString(), level3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());
                    }
                    catalog2Vo.setCatalog3List(catalog3Vos);

                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));

        return collect;
    }


    //TODO 产生堆外内存溢出：outOfDirectMemoryError
    //1.springboot2.0以后，默认使用lettuce作为操作redis的客户端，她使用netty进行网络通信
    //2.lettuce的bug导致netty堆外内存溢出，netty如果没有指定堆外内存大小，默认使用jvm的-Xmx
    //可以通过-Dio.netty.maxDirectMemory进行设置
    //解决方案，不能使用-Dio.netty.maxDirectMemory只去调大堆外内存
    //1.升级lettuce客户端
    //2.切换使用jedis客户端
    //redisTemplate
    //lettuce\jedis封装了操作redis的基础api，spring再次封装成redisTemplate
    // @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson2()
    {
        //给缓存中放json字符串，拿出的json字符串，还要逆转为能用的对象类型【序列化与反序列化】

        /**
         * 1.空结果缓存：解决缓存穿透
         * 2.设置过期时间（加随机值）：解决缓存雪崩
         * 3.加锁：解决缓存击穿
         */

        //1.加入缓存逻辑,缓存中寸的数据是json字符串
        //JSON跨语言，跨平台兼容的
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJson))
        {
            //2.缓存中没有，查询数据库
            Map<String, List<Catalog2Vo>> catalogJsonFromDB = getCatalogJsonFromDBWithRedisLock();

            return catalogJsonFromDB;
        }

        System.out.println("缓存命中，直接返回。。。。。。");

        //转为指定的对象
        Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJson,new TypeReference<Map<String, List<Catalog2Vo>>>(){});

        return result;
    }

    /**
     * 缓存里的数据如何和数据库里的保持一致
     * 缓存一致性
     * 1.双写模式
     * 2.失效模式
     * @return
     */
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBWithRedissonLock()
    {
        //1.锁的名字。锁的粒度，越细越快
        //锁的粒度：具体缓存的是某个数据，11-号商品，product-11-lock product-12-lock
        RLock lock = redissonClient.getLock("catalogJSON-lock");
        lock.lock();
        Map<String, List<Catalog2Vo>> dataFromDB;
        try{
            dataFromDB = getDataFromDB();
        }finally
        {
            lock.unlock();
        }

        return dataFromDB;


    }

    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDBWithRedisLock()
    {
        //1.占分布式锁，去redis占坑
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid,300,TimeUnit.SECONDS);

        if (lock)
        {
            System.out.println("获取分布式锁成功。。。。。。。。。");
            Map<String, List<Catalog2Vo>> dataFromDB;
            try{
                //加锁成功.....执行业务
                //2.设置过期时间，防止死锁，如果lock判断进入，在设置过期时间之前断电，仍然会导致死锁，在占锁的时候同时设置过期时间，保证这个操作的原子性
                // stringRedisTemplate.expire("lock",30,TimeUnit.SECONDS);

                dataFromDB = getDataFromDB();
            }finally
            {

                // stringRedisTemplate.delete("lock");//删除锁,如果在删除锁之前，锁过期被redis自动删除，其他线程抢占到锁，执行业务，当前线程在删除锁时会把其他线程的锁也删除，导致更多线程进入到业务流程

                //删锁：1.获取锁的值对比 2.对比成功后删锁，这两步必须是一个原子操作：lua脚本解锁
                // String lock1 = stringRedisTemplate.opsForValue().get("lock");
                // if (uuid.equals(lock1))
                // {
                //     //删除我自己的锁，一样会出现问题，考虑到网络交互的时间，如果当前线程执行完从redis获取锁之后，锁过期，自动删除，其他线程抢占锁，此时if判断仍然时当前线程加的锁，此时删除锁会删除其他线程的锁
                //     stringRedisTemplate.delete("lock");
                // }
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

                //删除锁
                Long lock1 = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }

            return dataFromDB;
        }else
        {
            System.out.println("获取分布式锁失败，等待重试。。。。。。。。。。。");
            //加锁失败。。。。重试
            //休眠100毫秒重试
            try
            {
                Thread.sleep(200L);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            return getCatalogJsonFromDBWithRedisLock();//自旋的方式

        }

    }

    private Map<String, List<Catalog2Vo>> getDataFromDB()
    {
        //得到锁以后应该再去缓存中确定一次，如果没有，再去数据库查询
        String catalogJson = stringRedisTemplate.opsForValue().get("catalogJson");
        if (!StringUtils.isEmpty(catalogJson))
        {
            System.out.println("缓存没有命中，查询数据库。。。。。。");
            //缓存不为null直接返回
            Map<String, List<Catalog2Vo>> result = JSON.parseObject(catalogJson, new TypeReference<Map<String, List<Catalog2Vo>>>(){});
            return result;
        }

        System.out.println("查询了数据库。。。。。。" + Thread.currentThread().getName());


        /**
         * 1.将数据库的多次查询变为一次
         *
         */
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);


        //1.查出所有一级分类
        List<CategoryEntity> level1Categories = getParent_cid(categoryEntities, 0L);

        //2.封装数据
        Map<String, List<Catalog2Vo>> collect = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v ->
        {
            //1.遍历每一个一级分类，查到这个一级分类的所有二级分类
            List<CategoryEntity> entityList = getParent_cid(categoryEntities, v.getCatId());
            //2.封装上面的结果
            List<Catalog2Vo> catalog2Vos = null;
            if (entityList != null)
            {
                catalog2Vos = entityList.stream().map(level2 ->
                {
                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), null, level2.getCatId().toString(), level2.getName());
                    //1.找当前二级分类下的三级分类
                    List<CategoryEntity> categoryLevel3Entities = getParent_cid(categoryEntities, level2.getCatId());
                    List<Catalog2Vo.Catalog3Vo> catalog3Vos = null;
                    if (categoryLevel3Entities != null)
                    {
                        catalog3Vos = categoryLevel3Entities.stream().map(level3 ->
                        {
                            Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo(level2.getCatId().toString(), level3.getCatId().toString(), level3.getName());
                            return catalog3Vo;
                        }).collect(Collectors.toList());
                    }
                    catalog2Vo.setCatalog3List(catalog3Vos);

                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));

        //3.查到的数据再放入缓存
        String toJSONString = JSON.toJSONString(collect);
        stringRedisTemplate.opsForValue().set("catalogJson", toJSONString, 1, TimeUnit.DAYS);
        return collect;
    }

    /**
     * 从数据库查询并封装分类数据
     * @return
     *
     */
    public /*synchronized*/ Map<String, List<Catalog2Vo>> getCatalogJsonFromDBWithLocalLock()
    {
        /**
         * 只要是同一把锁，就能锁住需要这个锁的所有线程
         * synchronized (this):springboot所有的组件在容器中都是单例的，
         * synchronized 加在方法上也可以
         */
        //TODO 本地锁：synchronized，JUC（lock），在分布式情况下，想要锁住所有服务，必须使用分布式锁
        synchronized (this){
            //得到锁以后应该再去缓存中确定一次，如果没有，再去数据库查询
            return getDataFromDB();
        }

    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> categoryEntities,Long parent_cid)
    {
        List<CategoryEntity> entities = categoryEntities.stream().filter(item -> item.getParentCid() == parent_cid).collect(Collectors.toList());
        return entities;
    }

    private List<CategoryEntity> getChildrens(CategoryEntity categoryEntity,List<CategoryEntity> all)
    {
        //我写的版本，需要频繁去数据库查找，性能不好
        // Long parentCId = categoryEntity.getCatId();
        // Integer catLevel = categoryEntity.getCatLevel();
        // if (catLevel == 3)
        // {
        //     return null;
        // }
        // CategoryEntity queryEntity = new CategoryEntity();
        // queryEntity.setParentCid(parentCId);
        // List<CategoryEntity> childrenEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>(queryEntity));
        //
        // for (CategoryEntity entity:childrenEntities)
        // {
        //     List<CategoryEntity> childrens = getChildrens(entity);
        //     entity.setChildren(childrens);
        // }
        //
        // return childrenEntities;

        //判断分类的层级，如果是最后一级分类：三级分类，直接返回，不进行后续操作
        Integer catLevel = categoryEntity.getCatLevel();
        if (catLevel == 3)
        {
            return null;
        }

        //获取当前分类的分类ID，当作父分类ID传入参数，获取子分类
        Long catId = categoryEntity.getCatId();
        List<CategoryEntity> childrenEntities = all.stream().filter(item -> item.getParentCid().equals(catId))//此处大坑
                //找到子菜单
                .map(menu -> {
                    List<CategoryEntity> childrens = getChildrens(menu, all);
                    menu.setChildren(childrens);
                    return menu;
                })
                //排序
                .sorted((menu1,menu2) -> (menu1.getSort() == null ? 0 :menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort()))
                .collect(Collectors.toList());
        return childrenEntities;
    }

}