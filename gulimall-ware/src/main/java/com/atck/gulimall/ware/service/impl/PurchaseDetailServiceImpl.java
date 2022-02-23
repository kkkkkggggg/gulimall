package com.atck.gulimall.ware.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atck.common.utils.PageUtils;
import com.atck.common.utils.Query;

import com.atck.gulimall.ware.dao.PurchaseDetailDao;
import com.atck.gulimall.ware.entity.PurchaseDetailEntity;
import com.atck.gulimall.ware.service.PurchaseDetailService;
import org.springframework.util.StringUtils;


@Service("purchaseDetailService")
public class PurchaseDetailServiceImpl extends ServiceImpl<PurchaseDetailDao, PurchaseDetailEntity> implements PurchaseDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         *    key: '华为',//检索关键字
         *    status: 0,//状态
         *    wareId: 1,//仓库id
         */
        QueryWrapper<PurchaseDetailEntity> queryWrapper = new QueryWrapper<>();

        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status))
        {
            queryWrapper.eq("status",status);
        }

        String wareId = (String)params.get("wareId");
        if (!StringUtils.isEmpty(wareId))
        {
            queryWrapper.eq("ware_id",wareId);
        }

        String key = (String)params.get("key");
        if (!StringUtils.isEmpty(key))
        {
            queryWrapper.or().eq("id",key).or().eq("sku_id",key).or().eq("purchase_id",key).or().eq("sku_num",key)
                    .or().eq("sku_price",key);
        }

        IPage<PurchaseDetailEntity> page = this.page(
                new Query<PurchaseDetailEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<PurchaseDetailEntity> listDetailByPurchaseId(Long id)
    {
        List<PurchaseDetailEntity> list = this.list(new QueryWrapper<PurchaseDetailEntity>().eq("purchase_id", id));
        return list;
    }

}