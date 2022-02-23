package com.atck.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.atck.common.constant.AuthServerConstant;
import com.atck.common.exception.BizCodeEnume;
import com.atck.common.utils.R;
import com.atck.gulimall.auth.feign.MemberFeignClient;
import com.atck.gulimall.auth.feign.ThirdPartFeignClient;
import com.atck.gulimall.auth.vo.UserLoginVo;
import com.atck.gulimall.auth.vo.UserRegistVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController
{
    @Autowired
    ThirdPartFeignClient feignClient;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignClient memberFeignClient;
    /**
     * 发送请求直接跳转到一个页面
     * SpringMvc viewController将请求和页面映射过来
     */
    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone)
    {
        //TODO 1.接口防刷
        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);

        if (!StringUtils.isEmpty(redisCode))
        {
            long l = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - l < 60000)
            {
                //60秒内不能再发
                return R.error(BizCodeEnume.VALID_SMS_CODE_EXCEPTION.getCode(),BizCodeEnume.VALID_SMS_CODE_EXCEPTION.getMsg());
            }
        }

        String code = UUID.randomUUID().toString().substring(0, 5);
        //2.验证码的再次校验：redis 存key-phone,value-code
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone,code + "_" + System.currentTimeMillis(),10, TimeUnit.MINUTES);
        //redis缓存验证码，防止同一个phone在60秒内再次发送验证码


        feignClient.sendCode(phone,code);
        return R.ok();
    }

    /**
     * TODO 重定向携带数据，利用session原理，将数据放在session中，只要跳到下一个页面取出数据以后，session里面的数据就会被删掉
     * TODO 1.分布式情况下session问题
     * RedirectAttributes 模拟重定向携带数据
     * @param vo
     * @param result
     * @param model
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result, Model model, RedirectAttributes redirectAttributes)
    {
        //注册成功后回到首页，回到登录页
        if (result.hasErrors())
        {
            // Map<String,String> errors = new HashMap<>();
            //
            // for (FieldError fieldError : result.getFieldErrors())
            // {
            //     String field = fieldError.getField();
            //     String defaultMessage = fieldError.getDefaultMessage();
            //     errors.put(field,defaultMessage);
            // }

            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            // model.addAttribute("errors",errors);
            redirectAttributes.addFlashAttribute("errors",errors);

            // Request method 'POST' not supported
            //用户注册 -》 /regist[post] ---》 转发/reg.html(路径映射默认都是get方式访问的)
            // return "forward:/reg.html";

            //校验出错，转发到注册页
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        //真正注册，调用远程服务进行注册
        //1.校验验证码
        String code = vo.getCode();
        String s = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (!StringUtils.isEmpty(s))
        {
            if (code.equals(s.split("_")[0]))
            {
                //删除验证码
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                //验证码通过，真正注册，调用远程服务进行注册
                R regist = memberFeignClient.regist(vo);

                if (regist.getCode() == 0)
                {
                    //成功
                    return "redirect:http://auth.gulimall.com/login.html";
                }else{
                    Map<String,String> errors = new HashMap<>();
                    errors.put("msg",regist.getData("msg",new TypeReference<String>(){}));
                    redirectAttributes.addFlashAttribute("errors",errors);
                    return "redirect:http://auth.gulimall.com/reg.html";
                }

            }else{
                Map<String,String> errors = new HashMap<>();
                errors.put("code","验证码错误");
                redirectAttributes.addFlashAttribute("errors",errors);
                //校验出错，转发到注册页
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        }else{
            Map<String,String> errors = new HashMap<>();
            errors.put("code","验证码错误");
            redirectAttributes.addFlashAttribute("errors",errors);
            //校验出错，转发到注册页
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        //注册成功回到首页，回到登录页
        // return "redirect/login.html";
    }

    /**
     * 登录
     * @param vo
     * @return
     */
    @PostMapping("/login")
    public String login(UserLoginVo vo,RedirectAttributes redirectAttributes)
    {
        //远程登录
        R r = memberFeignClient.login(vo);
        if (r.getCode() == 0)
        {
            //成功
            return "redirect:http://gulimall.com";
        }else{
            Map<String,String> errors = new HashMap<>();
            errors.put("msg",r.getData("msg",new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors",errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }



}
