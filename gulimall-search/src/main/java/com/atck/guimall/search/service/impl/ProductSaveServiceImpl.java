package com.atck.guimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atck.common.to.es.SkuEsModel;
import com.atck.guimall.search.config.ElasticSearchConfig;
import com.atck.guimall.search.constant.EsConstant;
import com.atck.guimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductSaveServiceImpl implements ProductSaveService
{
    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException
    {
        //保存到es
        //1.给es中建立索引，product，建立好映射关系

        //2.给es中保存数据,BulkRequest bulkRequest, RequestOptions options
        BulkRequest bulkRequest = new BulkRequest();

        for (SkuEsModel model : skuEsModels)
        {
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(model.getSkuId().toString());
            String s = JSON.toJSONString(model);
            indexRequest.source(s, XContentType.JSON);

            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, ElasticSearchConfig.COMMON_OPTIONS);

        //TODO 1、如果批量错误
        boolean b = bulk.hasFailures();
        BulkItemResponse[] items = bulk.getItems();
        List<String> collect = Arrays.stream(items).map(item -> item.getId()).collect(Collectors.toList());
        log.info("商品上架成功:{}",collect);

        //有错误返回false，无错误返回true
        return !b;
    }
}
