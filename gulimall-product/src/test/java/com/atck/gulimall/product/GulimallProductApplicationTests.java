package com.atck.gulimall.product;


import com.atck.common.utils.R;
import com.atck.gulimall.product.dao.AttrGroupDao;
import com.atck.gulimall.product.dao.SkuSaleAttrValueDao;
import com.atck.gulimall.product.entity.BrandEntity;
import com.atck.gulimall.product.entity.CategoryEntity;
import com.atck.gulimall.product.entity.SpuInfoDescEntity;
import com.atck.gulimall.product.service.BrandService;
import com.atck.gulimall.product.service.CategoryService;
import com.atck.gulimall.product.service.SpuInfoDescService;
import com.atck.gulimall.product.vo.SkuItemSaleAttrVo;
import com.atck.gulimall.product.vo.SkuItemVo;
import com.atck.gulimall.product.vo.SpuItemAttrGroupVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests
{
    @Autowired
    StringRedisTemplate redisTemplate;

    @Resource
    BrandService brandService;

    // @Resource
    // // OSSClient ossClient;

    @Resource
    CategoryService categoryService;

    @Resource
    SpuInfoDescService descService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    public void skuSaleAttrValueDao()
    {
        List<SkuItemSaleAttrVo> saleAttrsBySpuId = skuSaleAttrValueDao.getSaleAttrsBySpuId(1L);
        System.out.println(saleAttrsBySpuId);
    }
    @Test
    public void testAttrGroupDao()
    {
        List<SpuItemAttrGroupVo> group = attrGroupDao.getAttrGroupWithAttrsBySpuId(1L, 225L);
        System.out.println(group.toString());
    }
    @Test
    public void contextLoads()
    {
        BrandEntity brandEntity = new BrandEntity();
        // brandEntity.setName("华为");
        // brandService.save(brandEntity);
        // System.out.println("保存成功");

        // brandEntity.setName("hahaha");
        // brandEntity.setBrandId(1L);
        // brandService.updateById(brandEntity);
        // System.out.println("修改成功");

        // BrandEntity entity = brandService.getById(1);
        // System.out.println(entity.toString());

        // List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1));
        // list.forEach((item) -> System.out.println(item));

        List<CategoryEntity> list = categoryService.list();

        // List<CategoryEntity> menu = new ArrayList<>();
        //
        // for (int i = 0; i < list.size(); i++)
        // {
        //     CategoryEntity entity = list.get(i);
        //     if (entity.getParentCid() == 0)
        //     {
        //        menu.add(entity);
        //     }
        // }
        //
        // for (int i = 0; i < menu.size(); i++)
        // {
        //     CategoryEntity subMenu1 = menu.get(i);
        //     List<CategoryEntity> childrens = getChildrens(subMenu1, list);
        //     for (int j = 0; j < childrens.size(); j++)
        //     {
        //         CategoryEntity subMenu2 = childrens.get(j);
        //         List<CategoryEntity> childrens1 = getChildrens(subMenu2, list);
        //         subMenu2.setChildren(childrens1);
        //     }
        //     subMenu1.setChildren(childrens);
        // }

        List<CategoryEntity> childrens = getChildrensV2(list.get(list.size() - 1), list);

        System.out.println(childrens);

    }


    //递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root,List<CategoryEntity> all){

        List<CategoryEntity> children = all.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid().equals(root.getCatId());
        }).map(categoryEntity -> {
            //1、找到子菜单
            categoryEntity.setChildren(getChildrens(categoryEntity,all));
            return categoryEntity;
        }).sorted((menu1,menu2)->{
            //2、菜单的排序
            return (menu1.getSort()==null?0:menu1.getSort()) - (menu2.getSort()==null?0:menu2.getSort());
        }).collect(Collectors.toList());

        return children;
    }

    private List<CategoryEntity> getChildrensV2(CategoryEntity root,List<CategoryEntity> all)
    {
        if (root.getCatLevel() == 3)
        {
            return null;
        }

        List<CategoryEntity> childrens = new ArrayList<>();
        Long catId = root.getCatId();

        for (int i = 0; i < all.size(); i++)
        {
            CategoryEntity entity = all.get(i);
            Long parentCid = entity.getParentCid();
            Integer showStatus = entity.getShowStatus();
            if (parentCid.equals(catId) && showStatus == 1)
            {
                childrens.add(entity);
            }
        }
        return childrens;
    }


    @Test
    public void test1()
    {
        // Long i = 2L - 2L;
        // Long j = 1L - 1L;
        // Long i2 = new Long(0);
        // System.out.println(i == 0);
        // System.out.println(i == j);
        // System.out.println(i2 == i);
        // System.out.println(i2 == j);
        String s = "1";
        Long l = 1L;
        String source = "拉萨看得见啊螺丝扣的骄傲";
        try
        {
            Long aLong = Long.decode(s);
            System.out.println(l + "---------------" + aLong);
            System.out.println(l == aLong);
        }catch(Exception e)
        {
            System.out.println(source.contains(s));
        }



    }

    @Test
    public void test2() throws FileNotFoundException
    {
        // // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
        // String endpoint = "https://oss-cn-shanghai.aliyuncs.com";
        // // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        // String accessKeyId = "LTAI5tRuHAnWvanY7x41k5mB";
        // String accessKeySecret = "oaPEpVGREOAPcFZZR2cRIqiS6Dt1y6";
        //
        // // 创建OSSClient实例。
        // OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        //
        // // 创建PutObjectRequest对象。
        // // 依次填写Bucket名称（例如examplebucket）、Object完整路径（例如exampledir/exampleobject.txt）和本地文件的完整路径。Object完整路径中不能包含Bucket名称。
        // // 如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件。
        // PutObjectRequest putObjectRequest = new PutObjectRequest("gulimall-kkkkk", "kkkkk.jpg", new File("F:\\File\\wallpaper\\91014617_p0.jpg"));
        //
        // // 如果需要上传时设置存储类型和访问权限，请参考以下示例代码。
        // // ObjectMetadata metadata = new ObjectMetadata();
        // // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
        // // metadata.setObjectAcl(CannedAccessControlList.Private);
        // // putObjectRequest.setMetadata(metadata);
        //
        // // 上传文件。
        // ossClient.putObject(putObjectRequest);
        //
        // // 关闭OSSClient。
        // ossClient.shutdown();
        // FileInputStream fileInputStream = new FileInputStream("F:\\File\\wallpaper\\91803703_p0.jpg");
        // ossClient.putObject("gulimall-kkkkk", "test2.jpg", fileInputStream);
        //
        // ossClient.shutdown();


    }
    @Test
    public void test4()
    {
        // List<Long> catelogPath = categoryService.findCatelogPath(255L);
        // System.out.println(catelogPath);
        // SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        // spuInfoDescEntity.setSpuId(1L);
        // spuInfoDescEntity.setDecript("hahahahaha");
        //
        // descService.saveWithSpuId(spuInfoDescEntity);
        //
        // System.out.println(spuInfoDescEntity.getSpuId());

        // Integer code = R.ok().getCode();
        // System.out.println(code);

        // Map<String,Object> map = new HashMap<>();
        // map.put("hhhh","1");
        //
        // Object hhhh = map.get("hhhh");
        //
        // System.out.println(i);
        boolean empty = StringUtils.isEmpty("");
        System.out.println(empty);

    }

    @Test
    public void testRedis()
    {
        //保存
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        ops.set("hello","world_"+ UUID.randomUUID().toString());

        //查询
        String hello = ops.get("hello");

        System.out.println("之前保存的数据是"+hello);
    }

    @Test
    public void testRedisson()
    {
        System.out.println(redissonClient);
    }
}
