package com.atck.gulimall.product.service;

import com.atck.common.utils.PageUtils;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atck.gulimall.product.entity.CommentReplayEntity;

import java.util.Map;

/**
 * 商品评价回复关系
 *
 * @author kkkkk
 * @email chenk3166@gmail.com
 * @date 2021-12-31 15:58:26
 */
public interface CommentReplayService extends IService<CommentReplayEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

