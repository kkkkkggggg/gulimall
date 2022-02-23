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

        //TODO 确认采购单的状态是正确的才可以合并



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

        //2.改变采购单的状态
        this.updateBatchById(purchaseEntities);

        //3.改变采购项的状态
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
        //1.更新采购单的状态，如果所有采购项都采购完成，则更新为采购完成，否则改状态为采购错误
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
            //2.查询采购单中的采购需求
            PurchaseDetailEntity byId = purchaseDetailService.getById(item.getItemId());

            Integer status = item.getStatus();
            //设置采购需求的新状态
            byId.setStatus(status);
            //如果采购需求的状态是已完成则更新库存，如果是采购失败则不更新库存
            if (status == WareConstant.PurchaseDetailStatusEnum.FINISHED.getStatus())
            {
                //查询与当前采购需求skuId和wareId相同的库存信息，如果有则更新库存数量，如果没有则新建库存信息
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

                    //TODO 远程查询sku的名字，如果失败，整个事务无需回滚
                    //1.自己catch异常
                    //TODO 还可以用什么办法让异常出现以后不会回滚
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

        //更新采购需求的状态，不管是采购完成还是失败
        purchaseDetailService.updateBatchById(collect);


    }

}