package com.atck.gulimall.product;

import com.alibaba.cloud.nacos.NacosConfigAutoConfiguration;
import com.alibaba.cloud.nacos.discovery.configclient.NacosConfigServerAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 1.整合MyBatis-plus
 *      1)、导入依赖
 *       <!--        mybatis-plus-->
 *         <dependency>
 *             <groupId>com.baomidou</groupId>
 *             <artifactId>mybatis-plus-boot-starter</artifactId>
 *             <version>3.2.0</version>
 *         </dependency>
 *       2）、配置
 *          1.配置
 *              1）、导入数据库驱动
 *              2）、
 *          2.配置Mybatis-Plus
 *              1)、使用@MapperScan
 * 2.逻辑删除
 *      1.配置全局删除逻辑
 *      2.配置逻辑删除组件（mybatisplus3.1之后不再需要这一步）
 *      3.给实体类的逻辑删除标识字段加上@TableLogic注解
 * 3.JSR303
 *      1.给Bean添加校验注解:javax.validation.constraints,并定义自己的message提示
 *      2.开启校验功能@Valid
 *          效果：校验错误以后会有默认的响应
 *      3.给校验的Bean后紧跟BindingResult 会自动绑定校验结果
 *      4.分组校验
 *          1.	@NotBlank(message = "品牌名必须提交",groups = {AddGroup.class,UpdateGroup.class})
 *          给校验注解标注什么情况需要进行校验
 *          2.@Validated(UpdateGroup.class)
 *          2.默认没有指定分组的校验注解@NotBlank，在分组校验@Validated(UpdateGroup.class)情况下不生效，只会在controller中没有添加@Validated下生效
 *      5.自定义校验
 *          1.编写一个自定义的校验注解
 *          2.编写一个自定义的校验器
 *          3.关联自定义的校验注解和校验器
 *          @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
 *          @Retention(RUNTIME)
 *          @Documented
 *          @Constraint(validatedBy = { ListValueConstraintValidator.class【可以指定多个不同的校验器】})
 *          public @interface ListValue
 *          4.页面修改不重启服务器实时更新
 *              1.引入dev-tools
 *              2.修改完页面，ctrl shift F9 重新编译一下页面，代码配置改变，建议重启
 * 4.统一的异常处理
 * @ControllerAdvice
 *      1.编写异常处理类，使用@ControllerAdvice
 *      2.使用@ExceptionHandler标注方法可以处理异常
 * 5.模板引擎
 *      1..thymeleaf-starter：关闭缓存
 *      2.静态资源都放在static文件夹下就可以直接访问
 *      3.页面放在template下直接访问
 *          springboot
 * 6.整合redis
 *      1.引入data-redis-starter
 *      2.简单配置redis的host等信息
 *      3.使用springboot自动配置好的StringRedisTemplate来操作redis
 *      redis->map，存放数据key，数据值value
 * 7.整合redisson作为分布式锁等功能框架
 *      1.引入依赖
 *      <dependency>
 *             <groupId>org.redisson</groupId>
 *             <artifactId>redisson</artifactId>
 *             <version>3.12.0</version>
 *         </dependency>
 *      2.配置redisson
 * 8.整合springcache简化缓存开发
 *      1.引入依赖
 *          spring-boot-starter-data-redis，spring-boot-starter-cache
 *      2.写配置
 *          1.自动配置了哪些
 *              CacheAutoConfiguration会导入RedisCacheConfiguration
 *              自动配好了缓存管理器RedisCacheManager
 *          2.配置使用redis作为缓存
 *          3.测试使用缓存
 *          @Cacheable: Triggers cache population.：触发将数据保存到缓存的操作
 *          @CacheEvict: Triggers cache eviction.：触发数据从缓存删除的操作
 *          @CachePut: Updates the cache without interfering with the method execution.：不影响方法执行更新缓存
 *          @Caching: Regroups multiple cache operations to be applied on a method.：组合以上多个操作
 *          @CacheConfig: Shares some common cache-related settings at class-level.：在类基本共享缓存的配置
 *              1.开启缓存功能@EnableCacheing
 *              2.只需要使用注解就能完成缓存操作
 *          4.原理
 *              CacheAutoConfiguration
 *                      RedisCacheConfiguration
 *                              RedisCacheManager->初始化所有缓存->每个缓存决定使用什么配置
 *                              ->如果RedisCacheConfiguration有就用已有的，没有就用默认的规则->想改缓存的配置，给容器中放一个RedisCacheConfiguration即可
 *                              ->就会应用到当前
 */
@MapperScan("com.atck.gulimall.product.dao")
@EnableFeignClients("com.atck.gulimall.product.feign")
@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching
public class GulimallProductApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(GulimallProductApplication.class,args);
    }
}
