package com.atck.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atck.common.utils.PageUtils;
import com.atck.gulimall.order.entity.RefundInfoEntity;

import java.util.Map;

/**
 * 退款信息
 *
 * @author kkkkk
 * @email chenk3166@gmail.com
 * @date 2022-01-01 13:23:26
 */
public interface RefundInfoService extends IService<RefundInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

