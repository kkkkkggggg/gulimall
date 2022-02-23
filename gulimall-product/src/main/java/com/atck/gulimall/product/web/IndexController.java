package com.atck.gulimall.product.web;

import com.atck.gulimall.product.entity.CategoryEntity;
import com.atck.gulimall.product.service.CategoryService;
import com.atck.gulimall.product.vo.Catalog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController
{
    @Autowired
    RedissonClient redissonClient;

    @Autowired
    CategoryService categoryService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @GetMapping({"/","/index.html"})
    public String indexPage(Model model)
    {
        //TODO 1.查出所有的一级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categories();

        model.addAttribute("categories",categoryEntities);
        //视图解析器进行拼串
        //classpath:/tempaltes/ + 返回值 + .html
        return "index";
    }

    //index/catalog.json
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catalog2Vo>> getCatalogJson()
    {
        Map<String, List<Catalog2Vo>> catalogJson = categoryService.getCatalogJson();
        return catalogJson;
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello()
    {
        //1.获取一把锁，只要锁的名字一样，就是同一把锁
        RLock myLock = redissonClient.getLock("myLock");

        //2.加锁
        myLock.lock();//阻塞式等待，默认加的锁都是30秒时间
        // myLock.lock(10, TimeUnit.SECONDS);//10秒自动解锁，自动解锁时间一定要大于业务执行的时间，使用这个方法，在锁到期后不会自动续期
        //1.如果设置了锁的超时时间，就发送给redis执行脚本，进行占锁，默认超时就是我们定义的时间
        //2.如果未指定时间，就使用30*1000【LockWatchDogTimeout看门狗的默认时间】
        //只要占锁成功，就会启动一个定时任务【重新设置锁的过期时间，新的过期时间就是看门狗的默认时间】，每过3/1看门狗默认时间进行锁的过期时间续期

        //1)、锁的自动续期，如果业务超长，运行期间自动给锁续上新的30秒，不用担心业务时间长，锁到期自动删掉
        //2）、加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认在30s以后自动删除

        //最佳实战
        // myLock.lock(10, TimeUnit.SECONDS)，省掉了续期操作
        try
        {
            System.out.println("加锁成功，执行业务" + Thread.currentThread().getId());
            try
            {
                Thread.sleep(30000L);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        } finally
        {
            //3.解锁,假设解锁代码没有运行，redisson会不会出现死锁
            System.out.println("释放锁："+Thread.currentThread().getId());
            myLock.unlock();
        }

        return "hello";
    }

    /**
     * 保证一定能读到最新数据，写锁是一个排他锁（互斥锁），读锁是一个共享锁
     * 写锁没释放，读就必须等待
     * 读 + 读 ：相当于无锁，并发读，只会在redis中记录好，所有当前的读锁，他们都会同时加锁成功
     * 写 + 读 ：等待写锁释放
     * 写 + 写 ：阻塞方式
     * 读 + 写 ：有读锁，写也需要等待
     * 只要有写的存在，都必须等待，读锁存在，写锁需要等待
     * @return
     */
    @ResponseBody
    @GetMapping("/write")
    public String writeValue()
    {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        RLock rLock = readWriteLock.writeLock();
        System.out.println("写锁加锁成功------" + Thread.currentThread().getId());
        String s = "";

        try
        {
            //1.改数据加写锁，读数据加读锁

            rLock.lock();
            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
            redisTemplate.opsForValue().set("writeValue",s);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }finally
        {
           rLock.unlock();
           System.out.println("写锁解锁成功------" + Thread.currentThread().getId());
        }
        return s;
    }

    @ResponseBody
    @GetMapping("/read")
    public String readValue()
    {
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        //加读锁
        RLock rLock = readWriteLock.readLock();
        rLock.lock();
        System.out.println("读锁加锁成功------"+ Thread.currentThread().getId());
        String value = "";
        try
        {
            value = redisTemplate.opsForValue().get("writeValue");
            Thread.sleep(30000);
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            rLock.unlock();
            System.out.println("读锁解锁成功------" + Thread.currentThread().getId());
        }

        return value;
    }

    /**
     * 放假；锁门
     * 1班人走完
     * 所有班级人都走完了才可以锁大门
     */
    @GetMapping("/lockDoor")
    @ResponseBody
    public String lockDoor() throws InterruptedException
    {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.trySetCount(5);
        door.await();//等待闭锁都完成

        return "放假了";
    }

    @ResponseBody
    @GetMapping("/gogogo/{id}")
    public String gogogo(@PathVariable("id")Long id)
    {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.countDown();//计数减一
        return id + "班的人走完了";
    }

    /**
     * 车库停车
     * 3个车位
     * 来一辆车，占用一个车位
     * 走一辆车，释放一个车位
     */
    @ResponseBody
    @GetMapping("/park")
    public String park() throws InterruptedException
    {
        RSemaphore park = redissonClient.getSemaphore("park");
        // park.acquire();//获取一个信号（值）
        boolean b = park.tryAcquire();
        if (b)
        {
            //执行业务
        }else {
            return "error";
        }
        return "停车成功==>" + b;
    }

    @ResponseBody
    @GetMapping("/go")
    public String go()
    {
        RSemaphore park = redissonClient.getSemaphore("park");
        park.release();//释放一个车位
        return "一辆车离开车库";
    }
}
