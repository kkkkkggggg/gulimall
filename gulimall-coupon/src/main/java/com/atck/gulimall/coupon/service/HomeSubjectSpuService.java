package com.atck.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atck.common.utils.PageUtils;
import com.atck.gulimall.coupon.entity.HomeSubjectSpuEntity;

import java.util.Map;

/**
 * 专题商品
 *
 * @author kkkkk
 * @email chenk3166@gmail.com
 * @date 2022-01-01 12:50:01
 */
public interface HomeSubjectSpuService extends IService<HomeSubjectSpuEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

