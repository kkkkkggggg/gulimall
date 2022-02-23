package com.atck.gulimall.product.service.impl;

import com.atck.common.constant.ProductConstant;
import com.atck.common.utils.PageUtils;
import com.atck.common.utils.Query;
import com.atck.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atck.gulimall.product.dao.AttrDao;
import com.atck.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atck.gulimall.product.entity.AttrEntity;
import com.atck.gulimall.product.entity.ProductAttrValueEntity;
import com.atck.gulimall.product.service.AttrAttrgroupRelationService;
import com.atck.gulimall.product.service.AttrService;
import com.atck.gulimall.product.service.ProductAttrValueService;
import com.atck.gulimall.product.vo.AttrGroupWithAttrVo;
import com.atck.gulimall.product.vo.SkuItemVo;
import com.atck.gulimall.product.vo.SpuItemAttrGroupVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atck.gulimall.product.dao.AttrGroupDao;
import com.atck.gulimall.product.entity.AttrGroupEntity;
import com.atck.gulimall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {
    @Autowired
    AttrAttrgroupRelationDao relationDao;

    @Autowired
    AttrDao attrDao;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    AttrService attrService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    AttrAttrgroupRelationService relationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId)
    {
        String key = (String)params.get("key");
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<AttrGroupEntity>();
        if (!StringUtils.isEmpty(key))
        {
            wrapper.and((obj) -> {
                obj.eq("attr_group_id",key).or().like("attr_group_name",key);
            });
        }
        if (catelogId == 0)
        {
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper);
            return new PageUtils(page);
        }else
        {

            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params), wrapper.eq("catelog_id",catelogId));
            return new PageUtils(page);
        }
    }

    @Override
    public List<AttrGroupWithAttrVo> getAttrGroupWithAttrsByCatelogId(Long catelogId)
    {
        //1.查询分组信息
        List<AttrGroupEntity> groupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));


        List<AttrGroupWithAttrVo> attrGroupWithAttrVos = groupEntities.stream().map(item ->
        {
            AttrGroupWithAttrVo attrGroupWithAttrVo = new AttrGroupWithAttrVo();
            BeanUtils.copyProperties(item, attrGroupWithAttrVo);

            List<AttrEntity> attrs = attrService.getRelatedAttr(attrGroupWithAttrVo.getAttrGroupId());

            attrGroupWithAttrVo.setAttrs(attrs);



            return attrGroupWithAttrVo;
        }).collect(Collectors.toList());

        return attrGroupWithAttrVos;
    }

    @Override
    public List<SpuItemAttrGroupVo> getAttrGroupWithAttrBySpuId(Long spuId, Long catalogId)
    {
        //查出当前spu对应的所有属性的分组信息以及当前分组下的所有属性对应的值
        List<SpuItemAttrGroupVo> vos =  this.baseMapper.getAttrGroupWithAttrsBySpuId(spuId,catalogId);
        return vos;
    }


}