package com.atck.gulimall.thirdpart.controller;

import com.atck.common.utils.R;
import com.atck.gulimall.thirdpart.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/sms")
public class SmsSendController
{
    @Autowired
    SmsComponent smsComponent;
    /**
     * 提供给别的服务进行调用
     * @param phone
     * @param code
     * @return
     */
    @ResponseBody
    @GetMapping("/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code)
    {
        smsComponent.sendSmsCode(phone,code);

        return R.ok();
    }
}
