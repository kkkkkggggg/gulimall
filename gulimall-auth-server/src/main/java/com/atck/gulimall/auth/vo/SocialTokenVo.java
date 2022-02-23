package com.atck.gulimall.auth.vo;

import lombok.Data;

@Data
public class SocialTokenVo
{
    private String access_token;
    private String scope;
    private String token_type;
}
