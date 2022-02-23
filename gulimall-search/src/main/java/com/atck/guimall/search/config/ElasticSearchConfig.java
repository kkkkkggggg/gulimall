package com.atck.guimall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.HttpAsyncResponseConsumerFactory;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.alibaba.nacos.api.common.Constants.TOKEN;

/**
 * 1.导入依赖
 * 2.编写配置,给容器中注入一个RestHighLevelClient
 */
@Configuration
public class ElasticSearchConfig
{
    public static final RequestOptions COMMON_OPTIONS;

    static
    {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();

        // builder.addHeader("Authorization","Bearer" + TOKEN);
        // builder.setHttpAsyncResponseConsumerFactory(new HttpAsyncResponseConsumerFactory.HeapBufferedResponseConsumerFactory(30*1024*1024*1024));
        COMMON_OPTIONS = builder.build();
    }

    @Bean
    public RestHighLevelClient esREstClient()
    {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("192.168.56.10", 9200, "http")));
        return client;
    }
}
