package com.atck.gulimall.product.service;

import com.atck.common.utils.PageUtils;
import com.atck.gulimall.product.vo.Catalog2Vo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atck.gulimall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author kkkkk
 * @email chenk3166@gmail.com
 * @date 2021-12-31 15:58:26
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIDs(List<Long> asList);

    Long[] findCatelogPath(Long catelogId);

    void updateCascade(CategoryEntity category);

    List<Long> getCatelogPath(Long catelogId, List<Long> path);

    List<CategoryEntity> getLevel1Categories();


    Map<String, List<Catalog2Vo>> getCatalogJson();
}

