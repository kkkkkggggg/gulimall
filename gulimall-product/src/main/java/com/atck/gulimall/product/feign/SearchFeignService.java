package com.atck.gulimall.product.feign;

import com.atck.common.to.es.SkuEsModel;
import com.atck.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-search")
public interface SearchFeignService
{
    @PostMapping("/search/product")
    //上架商品
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
