package com.atck.guimall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面可能传递过来的参数
 * catalog3Id=225&keyword=小米&sort=saleCount_asc
 */
@Data
public class SearchParam
{
    private String keyword;//页面传递过来的全文匹配关键字
    private Long catalog3Id;//三级分类Id
    /**
     * sort=saleCount_asc/desc
     * sort=skuPrice_asc/desc
     * sort=hotScore_asc/desc
     */
    private String sort;//排序条件

    /**
     * 好多的过滤条件
     * hasStock（是否有货），skuPrice区间，brandId，catalogId，attrs
     * hasStock=0/1
     * skuPrice=1_500/_500/500_
     *
     */
    private Integer hasStock;
    private String skuPrice;
    private List<Long> brandId;//按照品牌进行筛选，可以多选
    private List<String> attrs;//按照属性进行筛选
    private Integer pageNum = 1;//页码

    private String _queryString;//原生的所有查询条件

}
