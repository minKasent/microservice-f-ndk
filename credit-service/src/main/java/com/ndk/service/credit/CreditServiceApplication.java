package com.ndk.service.credit;

import com.ndk.common.api.exception.config.EnableCommonExceptionHandling;
import com.ndk.common.api.swagger.EnableCommonOpenAPIWithSecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableCommonOpenAPIWithSecurity
@EnableCommonExceptionHandling
public class CreditServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditServiceApplication.class, args);
    }

}
