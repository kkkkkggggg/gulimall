package com.atck.gulimall.product.service;

import com.atck.common.utils.PageUtils;
import com.atck.gulimall.product.vo.BrandVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atck.gulimall.product.entity.BrandEntity;

import java.util.List;
import java.util.Map;

/**
 * 品牌
 *
 * @author kkkkk
 * @email chenk3166@gmail.com
 * @date 2021-12-31 15:58:26
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void updateDetail(BrandEntity brand);

    List<BrandEntity> getBrandsByIds(List<Long> brandIds);
}

