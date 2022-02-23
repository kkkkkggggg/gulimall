package com.atck.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atck.common.utils.PageUtils;
import com.atck.gulimall.coupon.entity.CouponSpuRelationEntity;

import java.util.Map;

/**
 * 优惠券与产品关联
 *
 * @author kkkkk
 * @email chenk3166@gmail.com
 * @date 2022-01-01 12:50:01
 */
public interface CouponSpuRelationService extends IService<CouponSpuRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

