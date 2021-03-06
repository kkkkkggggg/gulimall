package com.atck.guimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atck.common.to.es.SkuEsModel;
import com.atck.common.utils.Query;
import com.atck.common.utils.R;
import com.atck.guimall.search.config.ElasticSearchConfig;
import com.atck.guimall.search.constant.EsConstant;
import com.atck.guimall.search.feign.ProductFeignClient;
import com.atck.guimall.search.service.MallSearchService;
import com.atck.guimall.search.vo.AttrResVo;
import com.atck.guimall.search.vo.BrandVo;
import com.atck.guimall.search.vo.SearchParam;
import com.atck.guimall.search.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.InternalOrder;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MallSearchServiceImpl implements MallSearchService
{
    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private ProductFeignClient feignClient;

    /**
     * ???es????????????
     *
     * @param param
     * @return
     */
    @Override
    public SearchResult search(SearchParam param)
    {
        //1.??????????????????????????????dsl??????

        //2.??????????????????
        SearchRequest searchRequest = buildSearchRequest(param);

        //3.??????????????????
        SearchResult searchResult = null;
        try
        {
            SearchResponse response = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);

            //4.???????????????????????????????????????????????????
            searchResult = buildSearchResult(response, param);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return searchResult;
    }

    /**
     * ??????????????????
     *
     * @param response
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param)
    {
        SearchResult searchResult = new SearchResult();
        //1.??????????????????????????????
        SearchHits hits = response.getHits();

        List<SkuEsModel> esModels = new ArrayList<>();
        if (hits.getHits() != null && hits.getHits().length > 0)
        {
            for (SearchHit hit : hits.getHits())
            {
                String sourceAsString = hit.getSourceAsString();

                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if (StringUtils.isNotEmpty(param.getKeyword()))
                {
                    HighlightField skuTitle = hit.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    skuEsModel.setSkuTitle(string);
                }
                esModels.add(skuEsModel);
            }
        }
        searchResult.setProducts(esModels);

        //2.????????????????????????????????????????????????
        ParsedNested attrAgg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        List<SearchResult.AttrVo> attrVos = new ArrayList<>();
        for (Terms.Bucket bucket : attrIdAgg.getBuckets())
        {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            //1.????????????id
            attrVo.setAttrId(bucket.getKeyAsNumber().longValue());

            //2.???????????????
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attr_name_agg");
            attrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());

            //3.???????????????
            List<String> list = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(item ->
            {
                String keyAsString = ((Terms.Bucket) item).getKeyAsString();
                return keyAsString;
            }).collect(Collectors.toList());

            attrVo.setAttrValue(list);

            attrVos.add(attrVo);
        }


        searchResult.setAttrs(attrVos);
        //3.????????????????????????????????????????????????
        ParsedLongTerms brandAgg = response.getAggregations().get("brand_agg");
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        for (Terms.Bucket bucket : brandAgg.getBuckets())
        {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            //????????????id
            brandVo.setBrandId(bucket.getKeyAsNumber().longValue());
            //??????????????????
            ParsedStringTerms brandImgAgg = bucket.getAggregations().get("brand_img_agg");
            brandVo.setBrandImg(brandImgAgg.getBuckets().get(0).getKeyAsString());
            //???????????????
            ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brand_name_agg");
            brandVo.setBrandName(brandNameAgg.getBuckets().get(0).getKeyAsString());
            brandVos.add(brandVo);
        }
        searchResult.setBrands(brandVos);
        //4.????????????????????????????????????????????????
        ParsedLongTerms catalogAgg = response.getAggregations().get("catalog_agg");
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        List<? extends Terms.Bucket> buckets = catalogAgg.getBuckets();
        for (Terms.Bucket bucket : buckets)
        {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            //????????????id
            String keyAsString = bucket.getKeyAsString();
            catalogVo.setCatalogId(Long.parseLong(keyAsString));
            //???????????????
            ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
            catalogVo.setCatalogName(catalogNameAgg.getBuckets().get(0).getKeyAsString());
            catalogVos.add(catalogVo);
        }
        searchResult.setCatalogs(catalogVos);
        //=============??????????????????????????????

        //5.????????????-??????
        searchResult.setPageNum(param.getPageNum());
        //5.????????????-????????????
        long total = hits.getTotalHits().value;
        searchResult.setTotal(total);
        //5.????????????-?????????
        int totalPage = (int) total % EsConstant.PRODUCT_PAGESIZE == 0 ? (int) total / EsConstant.PRODUCT_PAGESIZE : (int) total / EsConstant.PRODUCT_PAGESIZE + 1;
        searchResult.setTotalPage(totalPage);

        List<Integer> pageNavs = new ArrayList<>();
        for (int i = 1; i <= totalPage; i++)
        {
            pageNavs.add(i);
        }

        searchResult.setPageNavs(pageNavs);

        //6.?????????????????????????????????
        if (param.getAttrs() != null && param.getAttrs().size() > 0)
        {
            List<SearchResult.NavVo> navVos = param.getAttrs().stream().map(attr ->
            {
                SearchResult.NavVo navVo = new SearchResult.NavVo();

                String[] s = attr.split("_");
                navVo.setNavValue(s[1]);
                R r = feignClient.attrInfo(Long.parseLong(s[0]));
                if (r.getCode() == 0)
                {
                    AttrResVo resVo = r.getData("attr", new TypeReference<AttrResVo>() {});

                    searchResult.getAttrIds().add(resVo.getAttrId());
                    navVo.setNavName(resVo.getAttrName());
                }else
                {
                    navVo.setNavName(s[0]);
                }

                //2.????????????????????????????????????????????????????????????????????????url?????????
                String replace = replaceQueryString(param, attr,"attrs");
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);

                return navVo;
            }).collect(Collectors.toList());

            searchResult.setNavs(navVos);
        }

        if (param.getBrandId() != null && param.getBrandId().size() > 0)
        {

            List<SearchResult.NavVo> navs = searchResult.getNavs();
            SearchResult.NavVo navVo = new SearchResult.NavVo();

            navVo.setNavName("??????");
            //????????????????????????
            R infos = feignClient.infos(param.getBrandId());

            if (infos.getCode() == 0)
            {
                List<BrandVo> brands = infos.getData("brands", new TypeReference<List<BrandVo>>() {});
                StringBuffer stringBuffer = new StringBuffer();
                String replace = "";
                for (BrandVo vo : brands)
                {
                    stringBuffer.append(vo.getName() + ";");
                    replace = replaceQueryString(param,vo.getBrandId() + "","brandId");
                }
                navVo.setNavValue(stringBuffer.toString());
                navVo.setLink("http://search.gulimall.com/list.html?" + replace);
            }
            navs.add(navVo);
        }

        //TODO ??????????????????????????????
        return searchResult;
    }

    private String replaceQueryString(SearchParam param, String value,String key)
    {
        String encode = null;
        try
        {
            encode = URLEncoder.encode(value, "UTF-8");
            encode.replace("+","%20");//??????????????????????????????java?????????
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return param.get_queryString().replace("&" + key +"=" + encode, "");
    }

    /**
     * ??????????????????
     *
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param)
    {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();//??????DSL?????????

        /**
         * ??????????????????????????????(??????????????????????????????????????????????????????)
         */
        //1.????????????query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        //1.1???must-????????????
        if (!StringUtils.isEmpty(param.getKeyword()))
        {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        //1.2???bool-filter-??????????????????id??????
        if (param.getCatalog3Id() != null)
        {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }

        //1.3???bool-filter-????????????id??????
        if (param.getCatalog3Id() != null && param.getBrandId() != null && param.getBrandId().size() > 0)
        {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }

        //1.4???bool-filter-?????????????????????????????????
        if (param.getAttrs() != null && param.getAttrs().size() > 0)
        {
            for (String attr : param.getAttrs())
            {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                String[] s = attr.split("_");
                String attrId = s[0];//???????????????id
                String[] attrValues = s[1].split(":");//?????????????????????????????????
                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                //?????????????????????????????????nestedQuery
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }

        }

        //1.5???bool-filter-??????????????????
        if (param.getHasStock() != null)
        {
            boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() == 1));
        }

        //1.6???bool-filter-????????????????????????
        if (!StringUtils.isEmpty(param.getSkuPrice()))
        {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");

            String skuPrice = param.getSkuPrice();

            String[] s = param.getSkuPrice().split("_");

            if (skuPrice.startsWith("_"))
            {
                rangeQuery.lte(s[1].toString());
            } else if (skuPrice.endsWith("_"))
            {
                rangeQuery.gte(s[0].toString());
            } else
            {
                System.out.println(s[0]);
                System.out.println(s[1]);
                rangeQuery.gte(s[0].toString()).lte(s[1].toString());
            }

            boolQuery.filter(rangeQuery);
        }

        //??????????????????????????????????????????
        sourceBuilder.query(boolQuery);

        /**
         * ????????????????????????
         */
        //2.1??????
        if (!StringUtils.isEmpty(param.getSort()))
        {
            String[] s = param.getSort().split("_");

            sourceBuilder.sort(s[0], StringUtils.equalsIgnoreCase(s[1], "asc") ? SortOrder.ASC : SortOrder.DESC);
        }
        //2.2??????
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);

        //2.3??????
        if (StringUtils.isNotEmpty(param.getKeyword()))
        {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            sourceBuilder.highlighter(highlightBuilder);
        }


        /**
         * ????????????
         */
        //1.????????????
        TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg");
        brandAgg.field("brandId");
        brandAgg.size(10);
        //1.1???????????????
        TermsAggregationBuilder brandNameAgg = AggregationBuilders.terms("brand_name_agg");
        brandNameAgg.field("brandName");
        brandAgg.subAggregation(brandNameAgg);
        //1.2??????????????????
        TermsAggregationBuilder brandImgAgg = AggregationBuilders.terms("brand_img_agg");
        brandImgAgg.field("brandImg");
        brandAgg.subAggregation(brandImgAgg);

        sourceBuilder.aggregation(brandAgg);

        //2.????????????
        TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg");
        catalogAgg.field("catalogId");
        catalogAgg.size(10);

        TermsAggregationBuilder catalogNameAgg = AggregationBuilders.terms("catalog_name_agg");
        catalogNameAgg.field("catalogName");
        catalogNameAgg.size(10);
        catalogAgg.subAggregation(catalogNameAgg);

        sourceBuilder.aggregation(catalogAgg);

        //3.????????????
        //3.1??????????????????
        NestedAggregationBuilder nestedAttrAgg = AggregationBuilders.nested("attr_agg", "attrs");
        //3.2?????????id??????
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg");
        attrIdAgg.field("attrs.attrId");

        //3.3??????????????????
        TermsAggregationBuilder attrNameAgg = AggregationBuilders.terms("attr_name_agg");
        attrNameAgg.field("attrs.attrName");
        attrIdAgg.subAggregation(attrNameAgg);
        //3.4??????????????????
        TermsAggregationBuilder attrValueAgg = AggregationBuilders.terms("attr_value_agg");
        attrValueAgg.field("attrs.attrValue");
        attrIdAgg.subAggregation(attrValueAgg);
        nestedAttrAgg.subAggregation(attrIdAgg);
        sourceBuilder.aggregation(nestedAttrAgg);

        System.out.println(sourceBuilder.toString());//???????????????dsl??????

        SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
        return searchRequest;
    }
}
