package com.atck.gulimall.member;

import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

// @RunWith(SpringRunner.class)
// @SpringBootTest
public class GulimallMemberApplicationTests
{
    @Test
    public void contextLoads()
    {
        //e10adc3949ba59abbe56e057f20f883e
        //抗修改性，彩虹表。 123456 -> e10adc3949ba59abbe56e057f20f883e ,会被暴力破解
        String s = DigestUtils.md5Hex("123456");

        //MD5不能直接进行密码加密存储

        //盐值加密,密码进行加密存储，加盐：$1$ + 8位字符
        //$1$5jN8uTlu$rzVBaZliCLV4kBPIzj.Hi/
        //$1$qqqqqqqq$AZofg3QwurbxV3KEOzwuI1
        // String s1 = Md5Crypt.md5Crypt("123456".getBytes(),"$1$qqqqqqqq");
        // System.out.println(s1);

        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

        //$2a$10$Xtssnw6sl0.nKw0S9W9yNe19IkU0mjDrdqqEONbrI55nGv4Onghsu
        String encode = bCryptPasswordEncoder.encode("123456");

        boolean matches = bCryptPasswordEncoder.matches("123456", "$2a$10$Xtssnw6sl0.nKw0S9W9yNe19IkU0mjDrdqqEONbrI55nGv4Onghsu");

        System.out.println(encode + "=>" + matches);


    }

    @Test
    public void test()
    {

    }
}
