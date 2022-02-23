package com.atck.gulimall.product.service.impl;

import com.atck.common.utils.PageUtils;
import com.atck.common.utils.Query;
import com.atck.gulimall.product.entity.SkuImagesEntity;
import com.atck.gulimall.product.entity.SpuInfoDescEntity;
import com.atck.gulimall.product.service.*;
import com.atck.gulimall.product.vo.SkuItemSaleAttrVo;
import com.atck.gulimall.product.vo.SkuItemVo;
import com.atck.gulimall.product.vo.SpuItemAttrGroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atck.gulimall.product.dao.SkuInfoDao;
import com.atck.gulimall.product.entity.SkuInfoEntity;
import org.springframework.util.StringUtils;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService imagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        //http://localhost:88/api/product/skuinfo/list?t=1642074688632&page=1&limit=10&key=&catelogId=0&brandId=0&min=0&max=0
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();

        Object catelogId = params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && Long.decode((String) catelogId) > 0)
        {
            queryWrapper.eq("catalog_id",catelogId);
        }

        Object brandId = params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && Long.decode((String) brandId) > 0)
        {
            queryWrapper.eq("brand_id",brandId);
        }

        Long min = Long.decode((String) params.get("min"));
        Long max = Long.decode((String) params.get("max"));

        if (max == 0 && min > 0)
        {
            queryWrapper.ge("price",min);
        }

        if (min == 0 && max > 0)
        {
            queryWrapper.le("price",max);
        }

        if (max > min)
        {
            queryWrapper.ge("price",min).le("price",max);
        }

        if (!StringUtils.isEmpty(params.get("key")))
        {
            queryWrapper.and(wrapper ->{
                wrapper.eq("sku_id",params.get("key")).or().like("sku_name",params.get("key"));
            });
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity)
    {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId)
    {
        List<SkuInfoEntity> skuInfoEntities = this.baseMapper.selectList(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        return skuInfoEntities;
    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException
    {
        SkuItemVo skuItemVo = new SkuItemVo();

        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() ->
        {
            //sku基本信息获取 pms_sku_info
            SkuInfoEntity skuInfoEntity = getById(skuId);
            skuItemVo.setInfo(skuInfoEntity);
            return skuInfoEntity;
        }, executor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync(skuInfoEntity ->
        {
            //获取spu的组合信息
            List<SkuItemSaleAttrVo> saleAttrVos = skuSaleAttrValueService.getSaleAttrsBySpuId(skuInfoEntity.getSpuId());
            skuItemVo.setSaleAttr(saleAttrVos);
        }, executor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync(skuInfoEntity ->
        {
            //获取spu的介绍
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(skuInfoEntity.getSpuId());
            skuItemVo.setDesc(spuInfoDescEntity);
        }, executor);

        CompletableFuture<Void> groupAttrFuture = infoFuture.thenAcceptAsync(skuInfoEntity ->
        {
            //获取spu的规格参数信息
            List<SpuItemAttrGroupVo> spuItemAttrGroupVoList = attrGroupService.getAttrGroupWithAttrBySpuId(skuInfoEntity.getSpuId(), skuInfoEntity.getCatalogId());
            skuItemVo.setGroupAttrs(spuItemAttrGroupVoList);
        }, executor);


        //sku的图片信息 pms_sku_img
        CompletableFuture<Void> imgFuture = CompletableFuture.runAsync(() ->
        {
            List<SkuImagesEntity> skuImagesEntities = imagesService.getImagesBySkuId(skuId);
            skuItemVo.setImgs(skuImagesEntities);
        }, executor);

        //等待所有任务都完成
        CompletableFuture.allOf(saleAttrFuture,descFuture,groupAttrFuture,imgFuture).get();


        return skuItemVo;
    }

}