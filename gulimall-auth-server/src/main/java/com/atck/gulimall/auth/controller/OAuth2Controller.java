package com.atck.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atck.common.utils.HttpUtils;
import com.atck.common.utils.R;
import com.atck.gulimall.auth.feign.MemberFeignClient;
import com.atck.gulimall.auth.vo.MemberRespVo;
import com.atck.gulimall.auth.vo.SocialTokenVo;
import com.atck.gulimall.auth.vo.SocialUserInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理社交登录请求
 */
@Controller
@Slf4j
public class OAuth2Controller
{

    @Autowired
    MemberFeignClient memberFeignClient;

    /**
     * 社交登录成功回调
     * @param code
     * @return
     * @throws Exception
     */
    @GetMapping("/oauth2/github/success")
    public String github(@RequestParam("code")String code, HttpSession session) throws Exception
    {
        Map<String,String> map = new HashMap<>();
        map.put("client_id","11c114f5d7d9e80184e0");
        map.put("client_secret","4b43592b07e804e4b2fcec602a615e8da9e58ea4");
        map.put("code",code);
        map.put("redirect_uri","http://auth.gulimall.com/oauth2/github/success");
        //1.根据code换取access_token
        HttpResponse post = HttpUtils.doPost("https://github.com", "/login/oauth/access_token", "post", null, null, map);

        //2.处理
        if (post.getStatusLine().getStatusCode() == 200)
        {
            //获取到了token
            String json = EntityUtils.toString(post.getEntity());
            String[] strings = json.split("&");
            SocialTokenVo tokenVo = new SocialTokenVo();
            tokenVo.setAccess_token(strings[0].split("=")[1]);
            tokenVo.setScope("");
            tokenVo.setToken_type(strings[2].split("=")[1]);

            //知道当前是哪个社交用户
            //获取社交用户的详细信息
            Map<String,String> headerMap = new HashMap<>();
            headerMap.put("Authorization","token " + tokenVo.getAccess_token());
            HttpResponse get = HttpUtils.doGet("https://api.github.com", "/user", "get", headerMap, null);
            if (get.getStatusLine().getStatusCode() == 200)
            {
                String infoJson = EntityUtils.toString(get.getEntity());
                SocialUserInfoVo socialUserInfoVo = JSON.parseObject(infoJson, SocialUserInfoVo.class);
                //远程调用会员服务的登录方法。将社交用户的信息传入
                //1.当前用户如果是第一次进网站，自动注册进来（为当前社交用户生成一个会员信息账号，以后通过这个社交账号就对应指定的会员）
                //登录或注册这个社交用户
                R r = memberFeignClient.oauthLogin(socialUserInfoVo);
                if (r.getCode() == 0)
                {
                    MemberRespVo memberRespVo = r.getData("data", new TypeReference<MemberRespVo>(){});
                    log.info("登录成功，用户信息：" + memberRespVo.toString());
                    //1.第一次使用session：命令浏览器保存一个JSESSIONID=sessionid的cookie
                    //以后浏览器访问哪个网站就会带上这个网站的cookie
                    //子域之间 gulimall.com auth.gulimall.com order,gulimall.com
                    //通知浏览器保存cookie的时候，指定域名为父域名，即使是子域名下的cookie，父域名也能使用
                    //2.登录成功跳回首页
                    session.setAttribute("loginUser",memberRespVo);
                    return "redirect:http://gulimall.com";
                }else {
                    //登录失败，回到登录页；
                    return "redirect:http://auth.gulimall.com/login.html";
                }
            }else{
                //登录失败，回到登录页；
                return "redirect:http://auth.gulimall.com/login.html";
            }
        }else{
            //登录失败，回到登录页；
            return "redirect:http://auth.gulimall.com/login.html";
        }



    }
}
