package com.atck.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.atck.common.constant.ProductConstant;
import com.atck.common.to.SkuHasStockVo;
import com.atck.common.to.SkuReductionTo;
import com.atck.common.to.SpuBoundTo;
import com.atck.common.to.es.SkuEsModel;
import com.atck.common.utils.PageUtils;
import com.atck.common.utils.Query;
import com.atck.common.utils.R;
import com.atck.gulimall.product.entity.*;
import com.atck.gulimall.product.feign.CouponFeignService;
import com.atck.gulimall.product.feign.SearchFeignService;
import com.atck.gulimall.product.feign.WareFeignService;
import com.atck.gulimall.product.service.*;
import com.atck.gulimall.product.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.atck.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;
    
    @Autowired
    AttrService attrService;
    
    @Autowired
    ProductAttrValueService attrValueService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();
        //http://localhost:88/api/product/spuinfo/list?t=1642063309771&page=1&limit=10


        if (!StringUtils.isEmpty(params.get("status")))
        {
            queryWrapper.eq("publish_status",params.get("status"));
        }


        if (!StringUtils.isEmpty(params.get("brandId")) && Long.decode((String) params.get("brandId")) > 0)
        {

            queryWrapper.eq("brand_id",params.get("brandId"));
        }

        if (!StringUtils.isEmpty(params.get("catelogId")) && Long.decode((String) params.get("catelogId")) > 0)
        {

            queryWrapper.eq("catalog_id",params.get("catelogId"));
        }

        if (!StringUtils.isEmpty(params.get("key")))
        {
            queryWrapper.and(wrapper ->{
                wrapper.eq("id",params.get("key")).or().like("spu_name",params.get("key"));
            });
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);

        //http://localhost:88/api/product/spuinfo/list?t=1642065813214&status=&key=&brandId=0&catelogId=0&page=1&limit=10



    }

    /**
     *
     * @param spuSaveVo
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo spuSaveVo)
    {
        //1.??????spu???????????????pms_spu_info
        SpuInfoEntity infoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo,infoEntity);
        infoEntity.setCreateTime(new Date());
        infoEntity.setUpdateTime(new Date());
        this.saveBaseSpuInfo(infoEntity);

        //2.??????spu???????????????:pms_spu_info_desc
        List<String> decript = spuSaveVo.getDecript();
        SpuInfoDescEntity descEntity = new SpuInfoDescEntity();
        descEntity.setSpuId(infoEntity.getId());
        descEntity.setDecript(String.join(",",decript));
        spuInfoDescService.saveSpuInfoDesc(descEntity);

        //3.??????spu????????????:pms_spu_images
        List<String> images = spuSaveVo.getImages();
        spuImagesService.saveImages(infoEntity.getId(),images);


        //4.??????spu???????????????:pms_product_attr_value
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();
        List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(attr ->
        {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setAttrId(attr.getAttrId());
            AttrEntity attrEntity = attrService.getById(attr.getAttrId());
            valueEntity.setAttrName(attrEntity.getAttrName());
            valueEntity.setAttrValue(attr.getAttrValues());
            valueEntity.setQuickShow(attr.getShowDesc());
            valueEntity.setSpuId(infoEntity.getId());

            return valueEntity;
        }).collect(Collectors.toList());

        attrValueService.saveProductAttr(productAttrValueEntities);
        //5.??????spu???????????????:gulimall_sms -> sms_spu_bounds
        Bounds bounds = spuSaveVo.getBounds();
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(bounds,spuBoundTo);
        spuBoundTo.setSpuId(infoEntity.getId());
        R r = couponFeignService.saveSpuBounds(spuBoundTo);
        if (r.getCode() != 0)
        {
            log.error("????????????spu??????????????????");
        }

        //5.????????????spu??????????????????sku??????
        List<Skus> skus = spuSaveVo.getSkus();
        if (skus != null && skus.size() > 0)
        {

            skus.forEach(sku -> {
                String defaultImg = "";
                for (Images image : sku.getImages())
                {
                    if (image.getDefaultImg() == 1)
                    {
                        defaultImg = image.getImgUrl();
                    }
                }
                
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku,skuInfoEntity);
                skuInfoEntity.setBrandId(infoEntity.getBrandId());
                skuInfoEntity.setCatalogId(infoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(infoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(defaultImg);

                //5.1).sku???????????????:pms_sku_info
                skuInfoService.saveSkuInfo(skuInfoEntity);

                Long skuId = skuInfoEntity.getSkuId();

                List<SkuImagesEntity> imagesEntities = sku.getImages().stream().map(img ->
                {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());

                    return skuImagesEntity;
                }).filter(
                        //??????true???????????????false????????????
                        entity -> !StringUtils.isEmpty(entity.getImgUrl())).collect(Collectors.toList());
                //5.2).sku???????????????:pms_sku_images
                //TODO ?????????????????????????????????

                skuImagesService.saveBatch(imagesEntities);


                List<Attr> attrs = sku.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrs.stream().map(attr ->
                {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                //5.3).sku?????????????????????:pms_sku_sale_attr_value
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);

                //5.4).sku???????????????????????????gulimall_sms -> sms_sku_ladder\sms_sku_full_reduction\sms_member_price

                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku,skuReductionTo);
                skuReductionTo.setSkuId(skuId);
                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal(0)) == 1)
                {
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (r1.getCode() != 0)
                    {
                        log.error("????????????spu??????????????????");
                    }
                }

            });
        }




    }

    @Override
    public void saveBaseSpuInfo(SpuInfoEntity infoEntity)
    {
        this.baseMapper.insert(infoEntity);
    }

    @Override
    public void productUp(Long spuId)
    {
        //1.????????????spuid???????????????sku???????????????????????????
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.getSkusBySpuId(spuId);

        //??????sku???id
        List<Long> skuIdList = skuInfoEntities.stream().map(item -> item.getSkuId()).collect(Collectors.toList());
        //hasStock???hotScore
        //TODO 1.??????????????????????????????????????????????????????
        Map<Long, Boolean> stockMap = null;
        try
        {
            R r = wareFeignService.hasStock(skuIdList);
            TypeReference<List<SkuHasStockVo>> typeReference = new TypeReference<List<SkuHasStockVo>>(){};
            stockMap = r.getData(typeReference).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, item -> item.getHasStock()));
        }catch (Exception e)
        {
            log.error("????????????????????????:??????{}",e);
        }




        //TODO 4.????????????sku?????????????????????
        /**
         * private Long attrId;
         *
         *         private String attrName;
         *
         *         private String attrValue;
         */
        List<ProductAttrValueEntity> attrValueEntities = attrValueService.getAttrsBySpuId(spuId);

        List<Long> attrIds = attrValueEntities.stream().map(attrValue -> attrValue.getAttrId()).collect(Collectors.toList());
        List<Long> searchAttrIds = attrService.selectSearchAttrs(attrIds);

        List<SkuEsModel.Attrs> attrsList = attrValueEntities.stream().filter(item -> searchAttrIds.contains(item.getAttrId())).map(item ->
        {
            SkuEsModel.Attrs attrs = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(item, attrs);
            return attrs;
        }).collect(Collectors.toList());

        Map<Long, Boolean> finalStockMap = stockMap;

        //2.????????????sku?????????
        List<SkuEsModel> upProducts = skuInfoEntities.stream().map(skuInfoEntity -> {
            //?????????????????????
            SkuEsModel skuEsModel = new SkuEsModel();
            BeanUtils.copyProperties(skuInfoEntity,skuEsModel);
            //skuPrice???skuImg???
            skuEsModel.setSkuPrice(skuInfoEntity.getPrice());
            skuEsModel.setSkuImg(skuInfoEntity.getSkuDefaultImg());

            //?????????????????????
            if (finalStockMap == null)
            {
                skuEsModel.setHasStock(true);
            }else
            {
                skuEsModel.setHasStock(finalStockMap.get(skuInfoEntity.getSkuId()));
            }

            //TODO 2.???????????? 0
            skuEsModel.setHotScore(0L);
            /**
             *  private String brandName;
             *
             *     private String brandImg;
             *
             *     private String catalogName;
             */
            //TODO 3.???????????????????????????????????????
            BrandEntity brandEntity = brandService.getById(skuInfoEntity.getBrandId());
            skuEsModel.setBrandName(brandEntity.getName());
            skuEsModel.setBrandImg(brandEntity.getLogo());
            CategoryEntity categoryEntity = categoryService.getById(skuInfoEntity.getCatalogId());
            skuEsModel.setCatalogName(categoryEntity.getName());

            //??????????????????
            skuEsModel.setAttrs(attrsList);

            return skuEsModel;
        }).collect(Collectors.toList());

        //TODO 5.??????????????????es????????????

        R r = searchFeignService.productStatusUp(upProducts);
        if (r.getCode() == 0)
        {
            //??????????????????
            //TODO 6.????????????spu?????????
            this.baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        }else{
            //??????????????????
            //TODO 7.?????????????????????????????????????????????
            //Feign????????????
            /**
             * 1.????????????????????????????????????json
             *
             * 2.????????????????????????
             *
             * 3.??????????????????????????????
             */
        }
    }


}