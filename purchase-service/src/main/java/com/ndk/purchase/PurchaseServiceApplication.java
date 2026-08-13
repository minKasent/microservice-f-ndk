package com.ndk.purchase;

import com.ndk.common.api.exception.config.EnableCommonExceptionHandling;
import com.ndk.common.api.swagger.EnableCommonOpenAPIWithSecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCommonOpenAPIWithSecurity
@EnableCommonExceptionHandling
@EnableFeignClients
@EnableAsync
public class PurchaseServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(PurchaseServiceApplication.class, args);
  }

}
