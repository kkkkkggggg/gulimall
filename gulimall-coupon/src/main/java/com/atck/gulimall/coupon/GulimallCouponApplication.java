package com.atck.gulimall.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 1.想要远程调用别的服务
 *      1）、引入openfeign
 *      2）、编写一个接口，告诉SpringCloud这个接口需要调用远程服务
 *          1.声明接口的每一个方法都是调用哪个远程服务的哪个请求
 *      3）、开启远程调用功能
 * 2.如何使用Nacos作为全局配置中心
 *      1）、引入依赖
 *      2）、创建一个bootstrap.properties配置文件
 *          spring.application.name=gulimall-coupon-service
 *          spring.cloud.nacos.config.server-addr=192.168.56.10:8848
 *      3）、需要给配置中心中添加一个配置，配置的DataID默认是：应用名.properties
 *      4）、给 应用名.properties 中添加任何配置
 *      5）、动态的获取配置
 *      @RefreshScope：动态获取并刷新配置
 *      ${配置名} 获取配置
 *      如果配置中心和当前应用的配置都配置了相同的配置，优先使用配置中心的配置
 * 细节：
 *      命名空间：
 *          默认：public（保留空间），默认新增的所有配置都在public空间
 *              1.开发、测试、生产环境都有不同的配置，利用命名空间来做环境隔离
 *                  注意：在bootstrap.properties：配置上，需要使用哪个命名空间下的配置
 *                  spring.cloud.nacos.config.namespace=210dc103-0ae6-44ef-b72b-7bb4701b5c0e
 *              2.每一个微服务之间互相隔离配置，每一个微服务都创建自己的命名空间，只加载自己命名空间下的全部配置
 *
 *      配置集：
 *          所有的配置的集合
 *
 *      配置集ID：
 *          类似文件名
 *              DataID
 *      配置分组：
 *          默认所有的配置集都属于：DEFAULT_GROUP
 * 每个微服务创建自己的命名空间，使用配置分组区分不同环境的配置
 * 3、同时加载多个配置集
 *      微服务的任何配置信息，配置文件都可以放在配置中心中
 *      只需要在bootstrap.properties说明加载配置中心中哪些配置文件即可
 * @Value，@ConfigurationProperties，以前SpringBoot任何方法从配置文件中获取值，都能使用
 * 配置中心有的优先使用配置中心的配置
 *
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GulimallCouponApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(GulimallCouponApplication.class,args);
    }
}
