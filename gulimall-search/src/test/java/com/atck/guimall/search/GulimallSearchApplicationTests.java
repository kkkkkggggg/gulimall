package com.atck.guimall.search;

import com.alibaba.fastjson.JSON;
import com.atck.guimall.search.config.ElasticSearchConfig;
import lombok.Data;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedAvg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallSearchApplicationTests
{
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    public void contextLoads() throws IOException
    {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
        // user.source("userName","zhangsan","age",18,"gender","男");
        User user = new User();
        user.setAge(18);
        user.setGender("男");
        user.setUserName("张三");
        indexRequest.source(JSON.toJSONString(user), XContentType.JSON);//要保存的内容

        //执行操作
        IndexResponse index = restHighLevelClient.index(indexRequest, ElasticSearchConfig.COMMON_OPTIONS);

        //提取有用的响应数据
        System.out.println(index);
    }

    @Data
    class  User
    {
        private String userName;
        private String gender;
        private Integer age;
    }

    @Test
    public void test1() throws IOException
    {
        SearchRequest searchRequest = new SearchRequest("bank");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("address","mill"));

        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(100);
        searchSourceBuilder.aggregation(ageAgg);
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        searchSourceBuilder.aggregation(balanceAvg);
        searchRequest.source(searchSourceBuilder);

        System.out.println(searchSourceBuilder.toString());

        SearchResponse search = restHighLevelClient.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
        // System.out.println(search);

        // SearchHit[] hits = search.getHits().getHits();
        // for (SearchHit hit : hits)
        // {
        //     String sourceAsString = hit.getSourceAsString();
        //     Account account = JSON.parseObject(sourceAsString, Account.class);
        //
        //     System.out.println(account);
        // }

        Aggregations aggregations = search.getAggregations();

        Terms terms =  aggregations.get("ageAgg");
        for (Terms.Bucket bucket : terms.getBuckets())
        {
            String keyAsString = bucket.getKeyAsString();
            System.out.println("年龄:" + keyAsString + "==>" + bucket.getDocCount());
        }

        Map<String, Aggregation> stringAggregationMap = aggregations.asMap();

        System.out.println(stringAggregationMap);
        ParsedAvg avg = (ParsedAvg) stringAggregationMap.get("balanceAvg");
        double value = avg.getValue();

        System.out.println(value);


    }

    @Test
    public void test3()
    {
        String[] s = "1_500".split("_");
        System.out.println(s.length);
    }

}
