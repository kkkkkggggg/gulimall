package com.atck.gulimall.ware;

import com.atck.gulimall.ware.entity.PurchaseEntity;
import com.atck.gulimall.ware.entity.WareSkuEntity;
import com.atck.gulimall.ware.service.PurchaseService;
import com.atck.gulimall.ware.service.WareSkuService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallWareApplicationTests
{
    @Resource
    WareSkuService wareSkuService;

    @Resource
    PurchaseService purchaseService;

    @Test
    public void contextLoads()
    {
        PurchaseEntity purchaseEntity = new PurchaseEntity();

        purchaseEntity.setPhone("kkkkk");

        boolean save = purchaseService.save(purchaseEntity);

        System.out.println(save);
    }

    @Test
    public void test()
    {
        // WareSkuEntity one = wareSkuService.getOne(new QueryWrapper<WareSkuEntity>().eq("sku_id", 10).eq("ware_id", 11));
        //
        // System.out.println(one);


    }

}
