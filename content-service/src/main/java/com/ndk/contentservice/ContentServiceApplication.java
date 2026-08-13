package com.ndk.contentservice;

import com.ndk.common.api.exception.config.EnableCommonExceptionHandling;
import com.ndk.common.api.swagger.EnableCommonOpenAPIWithSecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableCommonOpenAPIWithSecurity
@EnableCommonExceptionHandling
@EnableFeignClients
public class ContentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContentServiceApplication.class, args);
    }

}
