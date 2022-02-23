package com.atck.gulimall.member.service.impl;

import com.atck.gulimall.member.dao.MemberLevelDao;
import com.atck.gulimall.member.entity.MemberLevelEntity;
import com.atck.gulimall.member.exception.PhoneExistException;
import com.atck.gulimall.member.exception.UserNameExistException;
import com.atck.gulimall.member.vo.MemberLoginVo;
import com.atck.gulimall.member.vo.MemberRegisterVo;
import com.atck.gulimall.member.vo.SocialTokenVo;
import com.atck.gulimall.member.vo.SocialUserInfoVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atck.common.utils.PageUtils;
import com.atck.common.utils.Query;

import com.atck.gulimall.member.dao.MemberDao;
import com.atck.gulimall.member.entity.MemberEntity;
import com.atck.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void register(MemberRegisterVo vo)
    {
        MemberDao memberDao = this.baseMapper;
        MemberEntity memberEntity = new MemberEntity();

        //设置默认等级
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());

        //检查用户名和手机是否唯一，为了Controller能感知异常，异常机制
        checkPhoneUnique(vo.getPhone());
        checkUsernameUnique(vo.getUserName());


        memberEntity.setMobile(vo.getPhone());
        memberEntity.setUsername(vo.getUserName());

        //密码要进行加密存储
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);

        //其他默认信息

        //保存
        memberDao.insert(memberEntity);
    }


    /**
     * 检查用户名唯一
     * @param username
     * @return
     */
    @Override
    public void checkUsernameUnique(String username) throws UserNameExistException
    {
        MemberDao memberDao = this.baseMapper;
        Integer count = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count>0)
        {
            throw new UserNameExistException();
        }
    }

    /**
     * 检查电话号码唯一
     * @param phone
     * @return
     */
    @Override
    public void checkPhoneUnique(String phone) throws PhoneExistException
    {
        MemberDao memberDao = this.baseMapper;
        Integer count = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count>0)
        {
            throw new PhoneExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo)
    {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();

        //1.去数据库查询
        MemberDao memberDao = this.baseMapper;
        MemberEntity memberEntity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct).or().eq("mobile", loginacct));
        if (memberEntity == null)
        {
            //登录失败
            return null;
        }else{
            //1.获取到数据库的password
            String passwordDb = memberEntity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            boolean matches = passwordEncoder.matches(password, passwordDb);
            if (matches)
            {
                //登录成功
                return memberEntity;
            }else{
                return null;
            }
        }

    }

    @Override
    public MemberEntity login(SocialUserInfoVo vo)
    {
        //登录和注册逻辑合并
        String id = vo.getId();
        //1.判断当前社交用户是否已经登陆过系统
        MemberDao memberDao = this.baseMapper;
        MemberEntity memberEntity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", id));
        if (memberEntity != null)
        {
            //这个用户已经注册
            return memberEntity;
        }else{
            //2.没有查到当前社交用户对应的记录，就需要注册
            MemberEntity register = new MemberEntity();
            register.setUsername(vo.getLogin());
            register.setSocialUid(id);
            register.setGender(1);
            register.setNodeId(vo.getNode_id());
            memberDao.insert(register);
            return register;
        }
    }

}