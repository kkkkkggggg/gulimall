package com.atck.gulimall.member.service;

import com.atck.gulimall.member.exception.PhoneExistException;
import com.atck.gulimall.member.exception.UserNameExistException;
import com.atck.gulimall.member.vo.MemberLoginVo;
import com.atck.gulimall.member.vo.MemberRegisterVo;
import com.atck.gulimall.member.vo.SocialTokenVo;
import com.atck.gulimall.member.vo.SocialUserInfoVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atck.common.utils.PageUtils;
import com.atck.gulimall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author kkkkk
 * @email chenk3166@gmail.com
 * @date 2022-01-01 13:14:32
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(MemberRegisterVo vo);


    void checkUsernameUnique(String username) throws UserNameExistException;

    void checkPhoneUnique(String phone) throws PhoneExistException;

    MemberEntity login(MemberLoginVo vo);

    MemberEntity login(SocialUserInfoVo infoVo);
}

