package com.ndk.identityservice.client.config;

import com.ndk.identityservice.config.properties.CreditInternalBasicCredentialsProperties;
import feign.Logger;
import feign.Logger.Level;
import feign.RequestInterceptor;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CreditClientConfig {

  private final CreditInternalBasicCredentialsProperties properties;

  @Bean
  Logger.Level creditClientLoggerLevel() {
    return Level.BASIC;
  }

  @Bean
  public RequestInterceptor creditRequestInterceptorWithBasicAuth() {
    return requestTemplate -> {
      String username = properties.getUsername();
      String password = properties.getPassword();
      String authHeader = "Basic " + Base64.getEncoder()
          .encodeToString((username + ":" + password).getBytes());
      requestTemplate.header("Authorization", authHeader);
    };
  }
}
