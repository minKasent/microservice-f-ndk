package com.ndk.identityservice.client.config;

import com.ndk.identityservice.config.properties.NotificationInternalBasicCredentialsProperties;
import feign.Logger;
import feign.Logger.Level;
import feign.RequestInterceptor;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class NotificationClientConfig {
  private final NotificationInternalBasicCredentialsProperties notificationInternalBasicCredentialsProperties;

  @Bean
  Logger.Level notificationLoggerLevel() {
    return Level.BASIC;
  }
  @Bean
  public RequestInterceptor requestInterceptorWithBasicAuth() {
    return requestTemplate -> {
      String username = notificationInternalBasicCredentialsProperties.getUsername();
      String password = notificationInternalBasicCredentialsProperties.getPassword();
      String authHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
      requestTemplate.header("Authorization", authHeader);
    };
  }
}