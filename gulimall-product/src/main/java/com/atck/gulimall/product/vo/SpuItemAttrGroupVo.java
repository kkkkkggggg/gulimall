package com.atck.gulimall.product.vo;

import lombok.Data;

import java.util.List;

@Data
public class SpuItemAttrGroupVo
{
    private String attrGroupName;
    private Long attrGroupId;
    private List<Attr> attrVos;
}
