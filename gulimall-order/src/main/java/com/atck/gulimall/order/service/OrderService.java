package com.atck.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atck.common.utils.PageUtils;
import com.atck.gulimall.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author kkkkk
 * @email chenk3166@gmail.com
 * @date 2022-01-01 13:23:26
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

