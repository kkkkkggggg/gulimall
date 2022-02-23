package com.atck.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.atck.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atck.gulimall.product.entity.AttrEntity;
import com.atck.gulimall.product.service.AttrAttrgroupRelationService;
import com.atck.gulimall.product.service.AttrService;
import com.atck.gulimall.product.service.CategoryService;
import com.atck.gulimall.product.vo.AttrGroupRelationVo;
import com.atck.gulimall.product.vo.AttrGroupWithAttrVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atck.gulimall.product.entity.AttrGroupEntity;
import com.atck.gulimall.product.service.AttrGroupService;
import com.atck.common.utils.PageUtils;
import com.atck.common.utils.R;



/**
 * 属性分组
 *
 * @author kkkkk
 * @email chenk3166@gmail.com
 * @date 2021-12-31 17:11:18
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService relationService;

    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@PathVariable("catelogId") Long catelogId,@RequestParam Map<String, Object> params){
        PageUtils page = attrGroupService.queryPage(params,catelogId);
        return R.ok().put("page",page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

		Long catelogId = attrGroup.getCatelogId();

        Long[] catelogPath = categoryService.findCatelogPath(catelogId);
        attrGroup.setCatelogPath(catelogPath);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    ///product/attrgroup/{attrgroupId}/attr/relation

    @RequestMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId)
    {
        List<AttrEntity> attrEntities = attrService.getRelatedAttr(attrgroupId);

        return R.ok().put("data",attrEntities);
    }

    ///product/attrgroup/{attrgroupId}/noattr/relation
    //http://localhost:88/api/product/attrgroup/2/noattr/relation
    @RequestMapping("/{attrgroupId}/noattr/relation")
    public R noRelationAttr(@RequestParam Map<String,Object> params,@PathVariable("attrgroupId")Long attrgroupId)
    {
        PageUtils page = attrService.getNotRelatedAttr(params,attrgroupId);
        return R.ok().put("page",page);
    }

    ///product/attrgroup/attr/relation/delete
    @PostMapping("/attr/relation/delete")
    public R deleteGroupRelation(@RequestBody AttrGroupRelationVo[] vos)
    {
        attrService.deleteRelation(vos);

        return R.ok();
    }

    ///product/attrgroup/attr/relation
    @RequestMapping("/attr/relation")
    public R saveAttrRelation(@RequestBody List<AttrGroupRelationVo> relationVos)
    {
        if (relationVos == null || relationVos.size() == 0)
        {
            return R.error("请选择有效的属性进行关联");
        }
        relationService.saveAttrRelation(relationVos);
        return R.ok();
    }

    /**
     * /product/attrgroup/{catelogId}/withattr
     * 获取分类下所有分组&关联属性
     */
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWitnAttr(@PathVariable("catelogId") Long catelogId)
    {
        List<AttrGroupWithAttrVo> vos = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);

        return R.ok().put("data",vos);
    }

}
