package com.atck.gulimall.product.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atck.common.valid.AddGroup;
import com.atck.common.valid.UpdateGroup;
import com.atck.common.valid.UpdateStatusGroup;
import com.atck.gulimall.product.vo.BrandVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.atck.gulimall.product.entity.BrandEntity;
import com.atck.gulimall.product.service.BrandService;
import com.atck.common.utils.PageUtils;
import com.atck.common.utils.R;

import javax.validation.Valid;


/**
 * 品牌
 *
 * @author kkkkk
 * @email chenk3166@gmail.com
 * @date 2021-12-31 17:11:18
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    //@RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     * @Valid 给实体类添加校验注解后，需要在提交数据时添加@Valid注解开启该实体类数据的校验功能
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:brand:save")

    public R save(@Validated(AddGroup.class) @RequestBody @Valid BrandEntity brand/*, BindingResult result*/){
        // if (result.hasErrors())
        // {
        //     Map<String,String> map = new HashMap<>();
        //     //1.获取校验的错误结果
        //     result.getFieldErrors().forEach((item)->{
        //         //获取到错误提示
        //         String message = item.getDefaultMessage();
        //         //获取错误的属性的名字
        //         String field = item.getField();
        //         map.put(field,message);
        //     });
        //     return R.error(400,"提交的数据不合法").put("data",map);
        // }
		brandService.save(brand);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:brand:update")
    public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand){
		brandService.updateDetail(brand);

        return R.ok();
    }


    /**
     * 修改状态
     */
    @RequestMapping("/update/status")
    //@RequiresPermissions("product:brand:update")
    public R updateStatus(@Validated(UpdateStatusGroup.class) @RequestBody BrandEntity brand){
        brandService.updateById(brand);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

    @GetMapping("/infos")
    public R infos(@RequestParam("brandIds")List<Long> brandIds)
    {
        List<BrandEntity> brandVos = brandService.getBrandsByIds(brandIds);
        return R.ok().put("brands",brandVos);
    }

}
