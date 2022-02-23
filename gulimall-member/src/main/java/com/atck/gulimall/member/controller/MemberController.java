package com.atck.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.atck.common.exception.BizCodeEnume;
import com.atck.gulimall.member.exception.PhoneExistException;
import com.atck.gulimall.member.exception.UserNameExistException;
import com.atck.gulimall.member.feign.CouponFeignService;
import com.atck.gulimall.member.vo.MemberLoginVo;
import com.atck.gulimall.member.vo.MemberRegisterVo;
import com.atck.gulimall.member.vo.SocialTokenVo;
import com.atck.gulimall.member.vo.SocialUserInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atck.gulimall.member.entity.MemberEntity;
import com.atck.gulimall.member.service.MemberService;
import com.atck.common.utils.PageUtils;
import com.atck.common.utils.R;



/**
 * 会员
 *
 * @author kkkkk
 * @email chenk3166@gmail.com
 * @date 2022-01-01 13:14:32
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;

    @RequestMapping("/coupons")
    public R test()
    {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("zhangsan");

        R r = couponFeignService.memberCoupons();

        return R.ok().put("member",memberEntity).put("coupons",r.get("coupon"));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

    @PostMapping("/regist")
    public R regist(@RequestBody MemberRegisterVo vo)
    {
        try
        {
            memberService.register(vo);
        } catch (PhoneExistException e)
        {
            return R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(),BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        }catch (UserNameExistException e)
        {
            return R.error(BizCodeEnume.USER_EXIST_EXCEPTION.getCode(),BizCodeEnume.USER_EXIST_EXCEPTION.getMsg());
        }
        return R.ok();
    }


    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo)
    {
        MemberEntity entity = memberService.login(vo);
        if (entity!= null)
        {
            return R.ok();
        }else{
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getCode(),BizCodeEnume.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getMsg());
        }
    }

    @PostMapping("/oauth/login")
    public R oauthLogin(@RequestBody SocialUserInfoVo vo)
    {
        MemberEntity entity = memberService.login(vo);
        if (entity != null)
        {
            return R.ok().setData(entity);
        }else{
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getCode(),BizCodeEnume.LOGINACCT_PASSWORD_INVAILD_EXCEPTION.getMsg());
        }
    }


}
