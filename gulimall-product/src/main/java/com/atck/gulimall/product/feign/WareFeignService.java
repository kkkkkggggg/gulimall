package com.atck.gulimall.product.feign;

import com.atck.common.to.SkuHasStockVo;
import com.atck.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService
{
    /**
     * 1.R设计时时候可以加上泛型
     * 2.直接返回我们想要的数据
     * 3.自己封装想要的数据
     * @param skuIds
     * @return
     */
    @PostMapping("/ware/waresku/hasstock")
    R hasStock(@RequestBody List<Long> skuIds);
}
