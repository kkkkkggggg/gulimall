package com.atck.gulimall.product.service;

import com.atck.common.utils.PageUtils;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atck.gulimall.product.entity.SpuInfoDescEntity;

import java.util.Map;

/**
 * spu信息介绍
 *
 * @author kkkkk
 * @email chenk3166@gmail.com
 * @date 2021-12-31 15:58:26
 */
public interface SpuInfoDescService extends IService<SpuInfoDescEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSpuInfoDesc(SpuInfoDescEntity descEntity);

}

