package com.atck.gulimall.ware.service.impl;

import com.atck.common.to.SkuHasStockVo;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atck.common.utils.PageUtils;
import com.atck.common.utils.Query;

import com.atck.gulimall.ware.dao.WareSkuDao;
import com.atck.gulimall.ware.entity.WareSkuEntity;
import com.atck.gulimall.ware.service.WareSkuService;
import org.springframework.util.StringUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         * wareId: 123,//仓库id
         * skuId: 123//商品id
         */
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();

        String wareId = (String) params.get("wareId");
        String skuId = (String) params.get("skuId");

        if (!StringUtils.isEmpty(wareId))
        {
            queryWrapper.eq("ware_id",wareId);
        }

        if (!StringUtils.isEmpty(skuId))
        {
            queryWrapper.eq("sku_id",skuId);
        }

        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuHasStockVo> hasStock(List<Long> skuIds)
    {
        List<SkuHasStockVo> skuHasStockVos = skuIds.stream().map(skuId ->
        {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            //查询当前sku的总库存量
            //SELECT SUM(`stock` - `stock_locked`) FROM `wms_ware_sku` WHERE `sku_id` = 1
            Long count = baseMapper.getSkuStock(skuId);

            skuHasStockVo.setSkuId(skuId);
            skuHasStockVo.setHasStock(count == null ? false : count > 0);
            return skuHasStockVo;
        }).collect(Collectors.toList());

        return skuHasStockVos;
    }

}