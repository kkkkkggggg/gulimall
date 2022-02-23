package com.atck.gulimall.product.service.impl;

import com.atck.common.utils.PageUtils;
import com.atck.common.utils.Query;
import com.atck.gulimall.product.service.CategoryBrandRelationService;
import com.atck.gulimall.product.vo.BrandVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atck.gulimall.product.dao.BrandDao;
import com.atck.gulimall.product.entity.BrandEntity;
import com.atck.gulimall.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        //1.获取key
        String key = (String) params.get("key");
        QueryWrapper<BrandEntity> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(key))
        {
            wrapper.eq("brand_id",key).or().like("name",key);
        }

        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void updateDetail(BrandEntity brand)
    {
        //保证冗余字段的数据一致
        this.updateById(brand);
        if (!StringUtils.isEmpty(brand.getName()))
        {
            //同步更新其他关联表钟大哥数据
            categoryBrandRelationService.updateBrand(brand.getBrandId(),brand.getName());

            //TODO 更新其他关联表
        }
    }

    @Override
    public List<BrandEntity> getBrandsByIds(List<Long> brandIds)
    {
        List<BrandEntity> brandEntities = baseMapper.selectList(new QueryWrapper<BrandEntity>().in("brand_id", brandIds));
        return brandEntities;
    }

}