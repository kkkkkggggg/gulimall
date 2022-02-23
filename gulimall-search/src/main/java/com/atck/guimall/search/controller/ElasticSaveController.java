package com.atck.guimall.search.controller;

import com.atck.common.exception.BizCodeEnume;
import com.atck.common.to.es.SkuEsModel;
import com.atck.common.utils.R;
import com.atck.guimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
@Slf4j
@RequestMapping("/search")
@RestController
public class ElasticSaveController
{
    @Autowired
    ProductSaveService saveService;

    @PostMapping("/product")
    //上架商品
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels)
    {
        boolean b = false;
        try
        {
            boolean upStatus = saveService.productStatusUp(skuEsModels);
            b = upStatus;
        } catch (IOException e)
        {
            log.error("ElasticSaveController商品上架错误:{}",e);
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }

        if (b)
        {
            return R.ok();
        }else
        {
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(),BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }
    }
}
