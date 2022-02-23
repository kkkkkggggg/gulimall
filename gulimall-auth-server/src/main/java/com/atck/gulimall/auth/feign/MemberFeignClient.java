package com.atck.gulimall.auth.feign;

import com.atck.common.utils.R;
import com.atck.gulimall.auth.vo.SocialUserInfoVo;
import com.atck.gulimall.auth.vo.UserLoginVo;
import com.atck.gulimall.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignClient
{

    @PostMapping("/member/member/regist")
    public R regist(@RequestBody UserRegistVo vo);

    @PostMapping("/member/member/login")
    public R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth/login")
    public R oauthLogin(@RequestBody SocialUserInfoVo vo);
}
