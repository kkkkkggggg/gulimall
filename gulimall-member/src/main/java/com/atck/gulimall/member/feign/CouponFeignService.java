package com.atck.gulimall.member.feign;

import com.atck.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-coupon-service")
public interface CouponFeignService
{
    @RequestMapping("/coupon/coupon/member/list")
    R memberCoupons();
}
