package com.atck.gulimall.ware.service.impl;

import com.atck.common.constant.WareConstant;
import com.atck.common.utils.R;
import com.atck.gulimall.ware.entity.PurchaseDetailEntity;
import com.atck.gulimall.ware.entity.WareSkuEntity;
import com.atck.gulimall.ware.feign.ProductFeignService;
import com.atck.gulimall.ware.service.PurchaseDetailService;
import com.atck.gulimall.ware.service.WareSkuService;
import com.atck.gulimall.ware.vo.DonePurchaseVo;
import com.atck.gulimall.ware.vo.ItemVo;
import com.atck.gulimall.ware.vo.MergeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atck.common.utils.PageUtils;
import com.atck.common.utils.Query;

import com.atck.gulimall.ware.dao.PurchaseDao;
import com.atck.gulimall.ware.entity.PurchaseEntity;
import com.atck.gulimall.ware.service.PurchaseService;
import org.springframework.transaction.annotation.Transactional;


@Service("purchaseService")
public class PurchaseServiceImpl extends ServiceImpl<PurchaseDao, PurchaseEntity> implements PurchaseService {

    @Autowired
    PurchaseDetailService purchaseDetailService;

    @Autowired
    WareSkuService wareSkuService;

    @Autowired
    ProductFeignService productFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<PurchaseEntity> page = this.page(
                new Query<PurchaseEntity>().getPage(params),
                new QueryWrapper<PurchaseEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils getUnreceivedPurchase()
    {
        QueryWrapper<PurchaseEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", WareConstant.PurchaseStatusEnum.CREATED.getStatus()).or().eq("status", WareConstant.PurchaseStatusEnum.ASSIGNED.getStatus());


        IPage<PurchaseEntity> page = this.page(new Query<PurchaseEntity>().getPage(new HashMap<String, Object>()),queryWrapper);

        PageUtils pageUtils = new PageUtils(page);
        return pageUtils;
    }

    @Transactional
    @Override
    public void merge(MergeVo vo)
    {
        Long purchaseId = vo.getPurchaseId();
        List<Long> items = vo.getItems();

        if (purchaseId == null)
        {
            PurchaseEntity purchaseEntity = new PurchaseEntity();

            purchaseEntity.setCreateTime(new Date());
            purchaseEntity.setUpdateTime(new Date());
            purchaseEntity.setPriority(1);
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.CREATED.getStatus());
            this.save(purchaseEntity);

            purchaseId = purchaseEntity.getId();
        }

        //TODO ???????????????????????????????????????????????????



        Long finalPurchaseId = purchaseId;
        List<PurchaseDetailEntity> collect = items.stream().map(id ->
        {
            PurchaseDetailEntity entity = new PurchaseDetailEntity();
            entity.setId(id);
            entity.setPurchaseId(finalPurchaseId);
            entity.setStatus(WareConstant.PurchaseDetailStatusEnum.RECEIVED.getStatus());

            return entity;
        }).collect(Collectors.toList());

        purchaseDetailService.updateBatchById(collect);

        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(purchaseId);
        purchaseEntity.setUpdateTime(new Date());
        this.updateById(purchaseEntity);
    }

    /**
     *
     * @param purchaseIds
     */
    @Override
    public void receivePurchase(List<Long> purchaseIds)
    {

        List<PurchaseEntity> purchaseEntities = purchaseIds.stream().map(id ->
        {
            PurchaseEntity byId = this.getById(id);
            return byId;
        }).filter(item ->
        {
            if (item.getStatus() == WareConstant.PurchaseStatusEnum.CREATED.getStatus() || item.getStatus() == WareConstant.PurchaseStatusEnum.ASSIGNED.getStatus())
            {
                return true;
            }
            return false;
        }).map(item -> {
            item.setStatus(WareConstant.PurchaseStatusEnum.RECEIVED.getStatus());
            item.setUpdateTime(new Date());
            return item;
        }).collect(Collectors.toList());

        //2.????????????????????????
        this.updateBatchById(purchaseEntities);

        //3.????????????????????????
        purchaseEntities.forEach(item ->  {
            List<PurchaseDetailEntity> purchaseDetailEntities = purchaseDetailService.listDetailByPurchaseId(item.getId());
            List<PurchaseDetailEntity> collect = purchaseDetailEntities.stream().map(entity ->
            {
                PurchaseDetailEntity entity1 = new PurchaseDetailEntity();
                entity1.setId(entity.getId());
                entity1.setStatus(WareConstant.PurchaseDetailStatusEnum.RECEIVED.getStatus());
                return entity1;
            }).collect(Collectors.toList());
            purchaseDetailService.updateBatchById(collect);

        });


    }

    @Transactional
    @Override
    public void donePurchase(DonePurchaseVo vo)
    {
        //1.???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(vo.getId());

        List<ItemVo> list = vo.getItems().stream().filter(item -> item.getStatus() == WareConstant.PurchaseDetailStatusEnum.ERROR.getStatus()).collect(Collectors.toList());

        if (list == null || list.size() == 0)
        {
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.FINISHED.getStatus());
        }else
        {
            purchaseEntity.setStatus(WareConstant.PurchaseStatusEnum.ERROR.getStatus());
        }
        this.updateById(purchaseEntity);


        List<PurchaseDetailEntity> collect = vo.getItems().stream().map(item ->
        {
            //2.?????????????????????????????????
            PurchaseDetailEntity byId = purchaseDetailService.getById(item.getItemId());

            Integer status = item.getStatus();
            //??????????????????????????????
            byId.setStatus(status);
            //????????????????????????????????????????????????????????????????????????????????????????????????
            if (status == WareConstant.PurchaseDetailStatusEnum.FINISHED.getStatus())
            {
                //???????????????????????????skuId???wareId??????????????????????????????????????????????????????????????????????????????????????????
                Long skuId = byId.getSkuId();
                Long wareId = byId.getWareId();
                Integer skuNum = byId.getSkuNum();
                WareSkuEntity wareSkuEntity = wareSkuService.getOne(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
                if (wareSkuEntity != null)
                {
                    Integer stock = wareSkuEntity.getStock();
                    wareSkuEntity.setStock(skuNum + stock);
                    wareSkuService.updateById(wareSkuEntity);
                } else
                {
                    WareSkuEntity entity = new WareSkuEntity();
                    entity.setSkuId(skuId);
                    entity.setWareId(wareId);
                    entity.setStock(skuNum);

                    //TODO ????????????sku???????????????????????????????????????????????????
                    //1.??????catch??????
                    //TODO ?????????????????????????????????????????????????????????
                    try
                    {
                        R info = productFeignService.info(skuId);
                        Map<String,Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                        if (info.getCode() == 0)
                        {
                            entity.setSkuName((String) skuInfo.get("skuName"));
                        }
                    }catch (Exception e){

                    }


                    entity.setStockLocked(0);
                    wareSkuService.save(entity);
                }

            }
            return byId;
        }).collect(Collectors.toList());

        //???????????????????????????????????????????????????????????????
        purchaseDetailService.updateBatchById(collect);


    }

}