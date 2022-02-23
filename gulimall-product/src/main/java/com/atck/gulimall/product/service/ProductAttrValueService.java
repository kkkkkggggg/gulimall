package com.atck.gulimall.product.service;

import com.atck.common.utils.PageUtils;
import com.atck.gulimall.product.vo.SkuItemVo;
import com.atck.gulimall.product.vo.UpdateAttrValueVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atck.gulimall.product.entity.ProductAttrValueEntity;

import java.util.List;
import java.util.Map;

/**
 * spu属性值
 *
 * @author kkkkk
 * @email chenk3166@gmail.com
 * @date 2021-12-31 15:58:26
 */
public interface ProductAttrValueService extends IService<ProductAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveProductAttr(List<ProductAttrValueEntity> productAttrValueEntities);

    void batchUpdateSpuAttr(Long spuId, List<ProductAttrValueEntity> list);

    List<ProductAttrValueEntity> getAttrsBySpuId(Long spuId);

}

