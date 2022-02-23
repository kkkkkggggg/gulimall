package com.atck.gulimall.product.vo;

import lombok.Data;

@Data
public class UpdateAttrValueVo
{
    /**
     * "attrId": 7,
     * 	"attrName": "入网型号",
     * 	"attrValue": "LIO-AL00",
     * 	"quickShow": 1
     */
    private Long attrId;

    private String attrName;

    private String attrValue;

    private Integer quickShow;
}
