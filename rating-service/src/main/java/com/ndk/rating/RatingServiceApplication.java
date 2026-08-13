package com.ndk.rating;

import com.ndk.common.api.exception.config.EnableCommonExceptionHandling;
import com.ndk.common.api.swagger.EnableCommonOpenAPIWithSecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableCommonOpenAPIWithSecurity
@EnableCommonExceptionHandling
@EnableFeignClients
public class RatingServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(RatingServiceApplication.class, args);
  }

}
