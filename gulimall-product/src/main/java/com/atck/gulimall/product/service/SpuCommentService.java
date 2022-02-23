package com.atck.gulimall.product.service;

import com.atck.common.utils.PageUtils;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atck.gulimall.product.entity.SpuCommentEntity;

import java.util.Map;

/**
 * 商品评价
 *
 * @author kkkkk
 * @email chenk3166@gmail.com
 * @date 2021-12-31 15:58:26
 */
public interface SpuCommentService extends IService<SpuCommentEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

