package com.atck.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.atck.gulimall.product.entity.ProductAttrValueEntity;
import com.atck.gulimall.product.service.ProductAttrValueService;
import com.atck.gulimall.product.vo.AttrGroupRelationVo;
import com.atck.gulimall.product.vo.AttrResponseVo;
import com.atck.gulimall.product.vo.AttrVo;
import com.atck.gulimall.product.vo.UpdateAttrValueVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atck.gulimall.product.entity.AttrEntity;
import com.atck.gulimall.product.service.AttrService;
import com.atck.common.utils.PageUtils;
import com.atck.common.utils.R;



/**
 * 商品属性
 *
 * @author kkkkk
 * @email chenk3166@gmail.com
 * @date 2021-12-31 17:11:18
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService attrValueService;
    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }

    @RequestMapping("/{attrType}/list/{catelogId}")
    //@RequiresPermissions("product:attr:list")
    public R baseAtttrList(@RequestParam Map<String,Object> params,@PathVariable("catelogId") Long catelogId,@PathVariable("attrType") String attrType)
    {
        PageUtils page = attrService.queryBaseAttrPage(params,catelogId,attrType);

        return R.ok().put("page",page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{attrId}")
    //@RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId){
		// AttrEntity attr = attrService.getById(attrId);
        AttrResponseVo attrResponseVo = attrService.getAttrInfo(attrId);

        return R.ok().put("attr", attrResponseVo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attr:save")
    public R save( @RequestBody AttrVo attrVo){

		attrService.saveAttr(attrVo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrVo attrVo){
		attrService.updateAttr(attrVo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds){
		attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

    //product/attr/base/listforspu/{spuId}
    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrListForSpu(@PathVariable("spuId") Long spuId)
    {
        List<ProductAttrValueEntity> productAttrValueEntities =  attrValueService.list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id",spuId));
        return R.ok().put("data",productAttrValueEntities);
    }

    //product/attr/update/{spuId}
    @PostMapping("/update/{spuId}")
    public R updateSpuAttr(@PathVariable("spuId")Long spuId,@RequestBody List<ProductAttrValueEntity> list)
    {

        attrValueService.batchUpdateSpuAttr(spuId,list);

        return R.ok();
    }


}
