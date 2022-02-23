package com.atck.gulimall.product.dao;

import com.atck.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author kkkkk
 * @email chenk3166@gmail.com
 * @date 2021-12-31 15:58:26
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
