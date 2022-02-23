package com.atck.gulimall.auth;

import com.zaxxer.hikari.util.DriverDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import javax.sql.DataSource;

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class GulimallAuthServerApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(GulimallAuthServerApplication.class, args);
    }

}
