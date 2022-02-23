package com.atck.gulimall.ware.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.atck.gulimall.ware.vo.DonePurchaseVo;
import com.atck.gulimall.ware.vo.MergeVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atck.gulimall.ware.entity.PurchaseEntity;
import com.atck.gulimall.ware.service.PurchaseService;
import com.atck.common.utils.PageUtils;
import com.atck.common.utils.R;



/**
 * 采购信息
 *
 * @author kkkkk
 * @email chenk3166@gmail.com
 * @date 2022-01-01 13:30:57
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("ware:purchase:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("ware:purchase:info")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("ware:purchase:save")
    public R save(@RequestBody PurchaseEntity purchase){
		purchaseService.save(purchase);
        purchase.setCreateTime(new Date());
        purchase.setUpdateTime(new Date());
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("ware:purchase:update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("ware:purchase:delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    @PostMapping("/merge")
    public R merge(@RequestBody MergeVo vo)
    {
        purchaseService.merge(vo);

        return R.ok();
    }

    @GetMapping("/unreceive/list")
    public R getUnreceivedPurchase()
    {
        PageUtils pageUtils = purchaseService.getUnreceivedPurchase();

        return R.ok().put("page",pageUtils);
    }

    @PostMapping("/received")
    public R receivePurchase(@RequestBody List<Long> purchaseIds)
    {
        purchaseService.receivePurchase(purchaseIds);
        return R.ok();
    }

    @PostMapping("/done")
    public R donePurchase(@RequestBody DonePurchaseVo vo)
    {
        purchaseService.donePurchase(vo);
        return R.ok();
    }

}
