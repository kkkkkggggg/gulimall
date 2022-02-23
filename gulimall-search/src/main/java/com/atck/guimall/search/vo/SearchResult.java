package com.atck.guimall.search.vo;

import com.atck.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResult
{
    /**
     * 查到的商品信息
     */
    private List<SkuEsModel> products;

    //分页信息
    private Integer pageNum;//当前页码
    private Long total;//总记录数
    private Integer totalPage;//总页码
    private List<Integer> pageNavs;//页码数组

    private List<BrandVo> brands;//当前查询到的结果，所有涉及到的品牌
    private List<AttrVo> attrs;//当前查询到的结果，所有涉及到所有属性
    private List<CatalogVo> catalogs;//当前查询到的结果，所有涉及到所有分类

    //++++++++++++++以上是返回给页面的所有信息+++++++++++++++++++++++++++++++

    //面包屑导航数据
    private List<NavVo> navs = new ArrayList<>();
    private List<Long> attrIds = new ArrayList<>();

    @Data
    public static class NavVo
    {
        private String navName;
        private String navValue;
        private String link;
    }

    @Data
    public static class BrandVo
    {
        private Long brandId;
        private String brandName;
        private String brandImg;
    }

    @Data
    public static class AttrVo
    {
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    @Data
    public static class CatalogVo
    {
        private Long catalogId;
        private String catalogName;
    }
}
