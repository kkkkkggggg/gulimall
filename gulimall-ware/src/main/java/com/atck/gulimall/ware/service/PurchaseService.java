package com.atck.gulimall.ware.service;

import com.atck.gulimall.ware.vo.DonePurchaseVo;
import com.atck.gulimall.ware.vo.MergeVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atck.common.utils.PageUtils;
import com.atck.gulimall.ware.entity.PurchaseEntity;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author kkkkk
 * @email chenk3166@gmail.com
 * @date 2022-01-01 13:30:57
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils getUnreceivedPurchase();

    void merge(MergeVo vo);

    void receivePurchase(List<Long> purchaseIds);

    void donePurchase(DonePurchaseVo vo);
}

