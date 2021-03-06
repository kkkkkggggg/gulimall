package com.atck.gulimall.product.service.impl;

import com.atck.common.constant.ProductConstant;
import com.atck.common.utils.PageUtils;
import com.atck.common.utils.Query;
import com.atck.common.valid.ListValue;
import com.atck.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atck.gulimall.product.dao.AttrGroupDao;
import com.atck.gulimall.product.dao.CategoryDao;
import com.atck.gulimall.product.entity.*;
import com.atck.gulimall.product.service.AttrAttrgroupRelationService;
import com.atck.gulimall.product.service.CategoryService;
import com.atck.gulimall.product.service.ProductAttrValueService;
import com.atck.gulimall.product.vo.AttrGroupRelationVo;
import com.atck.gulimall.product.vo.AttrResponseVo;
import com.atck.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atck.gulimall.product.dao.AttrDao;
import com.atck.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationDao relationDao;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    CategoryService categoryService;

    @Autowired
    ProductAttrValueService attrValueService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attrVo)
    {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo,attrEntity);

        //1.??????????????????
        this.save(attrEntity);

        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attrVo.getAttrGroupId() != null)
        {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(attrEntity.getAttrId());
            relationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            //2.??????????????????
            relationDao.insert(relationEntity);
        }

    }

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId,String attrType)
    {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("attr_type","base".equalsIgnoreCase(attrType) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode():
                ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        if (catelogId != 0)
        {
            queryWrapper.eq("catelog_id",catelogId);
        }

        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key))
        {
            queryWrapper.and((wrapper)->{
                wrapper.eq("attr_id",key).or().like("attr_name",key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params),queryWrapper);
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        List<AttrResponseVo> responseVos = records.stream().map(attrEntity ->
        {
            AttrResponseVo responseVo = new AttrResponseVo();
            BeanUtils.copyProperties(attrEntity, responseVo);

            if ("base".equalsIgnoreCase(attrType))
            {
                //??????????????????????????????
                AttrAttrgroupRelationEntity relationEntity = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrEntity.getAttrId()));
                if (relationEntity != null && relationEntity.getAttrGroupId() != null)
                {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                    responseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }

            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());

            if (categoryEntity != null)
            {
                responseVo.setCatelogName(categoryEntity.getName());
            }


            return responseVo;
        }).collect(Collectors.toList());
        pageUtils.setList(responseVos);
        return pageUtils;
    }

    @Cacheable(value = "attr",key = "'attrinfo' + #root.args[0]")
    @Override
    public AttrResponseVo getAttrInfo(Long attrId)
    {
        AttrEntity attrEntity = this.getById(attrId);
        List<Long> path = new ArrayList<>();

        //1.????????????????????????
        List<Long> parentPath = categoryService.getCatelogPath(attrEntity.getCatelogId(), path);
        Collections.reverse(parentPath);

        AttrResponseVo attrResponseVo = new AttrResponseVo();
        BeanUtils.copyProperties(attrEntity,attrResponseVo);

        // //????????????????????????????????????????????????????????????valueType??????,?????????????????????????????????value_type????????????????????????
        // if (attrEntity.getValueSelect().split(";").length>1)
        // {
        //     attrResponseVo.setValueType(0);
        // }else
        // {
        //     attrResponseVo.setValueType(1);
        // }


        //????????????????????????
        attrResponseVo.setCatelogPath(parentPath.toArray(new Long[0]));

        CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
        if (categoryEntity != null)
        {
            attrResponseVo.setCatelogName(categoryEntity.getName());
        }

        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode())
        {
            //2.??????????????????
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = relationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if(attrAttrgroupRelationEntity!=null)
            {
                //??????????????????Id
                attrResponseVo.setAttrGroupId(attrAttrgroupRelationEntity.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectOne(new QueryWrapper<AttrGroupEntity>().eq("attr_group_id", attrAttrgroupRelationEntity.getAttrGroupId()));
                if (attrGroupEntity!=null)
                {
                    //????????????????????????
                    attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }


        return attrResponseVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attrVo)
    {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo,attrEntity);
        this.updateById(attrEntity);

        if (attrEntity.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode())
        {
            //1.??????????????????
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            relationEntity.setAttrId(attrVo.getAttrId());

            Integer count = relationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId()));

            if (count > 0)
            {
                relationDao.update(relationEntity,new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attrVo.getAttrId()));
            }else
            {
                relationDao.insert(relationEntity);
            }
        }


    }

    /**
     * ????????????id?????????????????????
     * @param attrgroupId
     * @return
     */
    @Override
    public List<AttrEntity> getRelatedAttr(Long attrgroupId)
    {

        List<AttrAttrgroupRelationEntity> relationEntities = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", attrgroupId));

        List<Long> attrIds = relationEntities.stream().map(attr ->
        {
            return attr.getAttrId();
        }).collect(Collectors.toList());

        if (attrIds == null || attrIds.size() == 0)
        {
            return null;
        }
        List<AttrEntity> attrEntities = (List<AttrEntity>) this.listByIds(attrIds);


        return attrEntities;

    }

    @Override
    public void deleteRelation(AttrGroupRelationVo[] vos)
    {
        // relationDao.delete(new QueryWrapper<>().eq("attr_id",1L).eq("attr_group_id",1L));
        List<AttrAttrgroupRelationEntity> relationEntities = Arrays.asList(vos).stream().map(item ->
        {
            AttrAttrgroupRelationEntity entity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item,entity);
            return entity;
        }).collect(Collectors.toList());
        relationDao.deleteBatchRelation(relationEntities);
    }

    // @Override
    // public PageUtils getNotRelatedAttr(Map<String, Object> params, Long attrgroupId)
    // {
    //     //1.???????????????????????????????????????Id
    //     AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
    //     //2.?????????????????????????????????????????????????????????????????????????????????
    //     QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()).eq("catelog_id", attrGroupEntity.getCatelogId());
    //     IPage<AttrEntity> page = this.baseMapper.selectPage(new Query<AttrEntity>().getPage(params), queryWrapper);
    //
    //     List<AttrEntity> allBaseAttrList = page.getRecords();
    //
    //     //3.????????????????????????????????????????????????
    //     List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntities = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>());
    //     //4.????????????????????????????????????Id
    //     List<Long> haveRelationAttrIds = attrAttrgroupRelationEntities.stream().map(item -> item.getAttrId()).collect(Collectors.toList());
    //
    //     //5.???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
    //     List<AttrEntity> noRelationAttr = allBaseAttrList.stream().filter(item -> !haveRelationAttrIds.contains(item.getAttrId())).collect(Collectors.toList());
    //
    //     if (noRelationAttr == null || noRelationAttr.size() == 0)
    //     {
    //         page.setRecords(null);
    //         page.setTotal(0);
    //         return new PageUtils(page);
    //     }else
    //     {
    //         page.setTotal(noRelationAttr.size());
    //         page.setRecords(noRelationAttr);
    //         return new PageUtils(page);
    //     }
    //
    // }

    @Override
    public PageUtils getNotRelatedAttr(Map<String, Object> params, Long attrgroupId)
    {
        //1.??????????????????????????????????????????????????????????????????
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        //2.?????????????????????????????????????????????????????????
        //2.1????????????????????????????????????
        List<AttrGroupEntity> groupEntityList = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<Long> attrGroupIdList = groupEntityList.stream().map(item -> item.getAttrGroupId()).collect(Collectors.toList());
        //2.2)\???????????????????????????
        List<AttrAttrgroupRelationEntity> relationEntities = relationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().in("attr_group_id", attrGroupIdList));
        List<Long> attrIdList = relationEntities.stream().map(item -> item.getAttrId()).collect(Collectors.toList());
        //2.3)??????????????????????????????????????????????????????
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<AttrEntity>().eq("catelog_id", catelogId).eq("attr_type",ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());

        if (attrIdList != null && attrIdList.size() > 0)
        {
            queryWrapper.notIn("attr_id", attrIdList);
        }
        String key = (String)params.get("key");
        if (!StringUtils.isEmpty(key))
        {
            queryWrapper.and(wrapper -> {
                wrapper.eq("attr_id",key).or().like("attr_name",key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper);

        PageUtils pageUtils = new PageUtils(page);


        return pageUtils;


    }

    @Override
    public List<Long> selectSearchAttrs(List<Long> attrIds)
    {
        return baseMapper.selectSearchAttrIds(attrIds);
    }


}