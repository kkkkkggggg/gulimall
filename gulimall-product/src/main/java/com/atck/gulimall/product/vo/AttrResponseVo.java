package com.atck.gulimall.product.vo;

import com.atck.gulimall.product.entity.AttrEntity;
import lombok.Data;

@Data
public class AttrResponseVo extends AttrVo
{
    /**
     * "catelogName": "手机/数码/手机", //所属分类名字
     * 			"groupName": "主体", //所属分组名字
     */



    private String catelogName;

    private String groupName;

    private Long[] catelogPath;

    // /**
    //  * 属性分组Id
    //  */
    // private Long attrGroupId;

}
