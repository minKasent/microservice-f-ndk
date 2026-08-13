package com.ndk.notificationservice;

import com.ndk.common.api.exception.config.EnableCommonExceptionHandling;
import com.ndk.common.api.swagger.EnableCommonOpenAPIWithSecurity;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCommonOpenAPIWithSecurity
@EnableCommonExceptionHandling
@EnableAsync
public class NotificationServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(NotificationServiceApplication.class, args);
  }

}
