package com.atck.gulimall.product.vo;

import com.atck.gulimall.product.entity.SkuImagesEntity;
import com.atck.gulimall.product.entity.SkuInfoEntity;
import com.atck.gulimall.product.entity.SpuInfoDescEntity;
import jdk.nashorn.internal.runtime.linker.LinkerCallSite;
import lombok.Data;
import org.aspectj.lang.annotation.DeclareAnnotation;

import java.util.List;
@Data
public class SkuItemVo
{
    //sku基本信息获取 pms_sku_info
    private SkuInfoEntity info;
    //sku的图片信息 pms_sku_img
    private List<SkuImagesEntity> imgs;
    //获取sku的组合信息
    List<SkuItemSaleAttrVo> saleAttr;
    //获取spu的介绍
    private SpuInfoDescEntity desc;
    //获取spu的规格参数信息
    List<SpuItemAttrGroupVo> groupAttrs;

    boolean hasStock = true;

}
