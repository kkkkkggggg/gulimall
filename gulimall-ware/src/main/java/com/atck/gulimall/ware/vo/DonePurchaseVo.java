package com.atck.gulimall.ware.vo;

import lombok.Data;

import java.util.List;
@Data
public class DonePurchaseVo
{
    private Long id;

    private List<ItemVo> items;


}
