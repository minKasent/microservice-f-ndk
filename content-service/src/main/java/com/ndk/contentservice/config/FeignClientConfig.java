package com.ndk.contentservice.config;

import feign.Logger;
import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Feign Client Configuration with Bearer Token Interceptor
 * 
 * Since each service is a Resource Server, when service A calls service B:
 * - Service A receives request with Bearer token from user
 * - Service A needs to forward this Bearer token when calling service B via Feign
 * - Service B validates the token as a Resource Server
 */
@Configuration
@Slf4j
public class FeignClientConfig {

  /**
   * Request Interceptor to forward Bearer token from current request context
   * to outgoing Feign requests
   */
  @Bean
  public RequestInterceptor bearerTokenRequestInterceptor() {
    return requestTemplate -> {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      
      if (authentication instanceof JwtAuthenticationToken jwtAuth) {
        String tokenValue = jwtAuth.getToken().getTokenValue();
        requestTemplate.header("Authorization", "Bearer " + tokenValue);
        log.debug("Forwarding Bearer token to Feign request: {}", requestTemplate.url());
      } else {
        log.warn("No JWT token found in SecurityContext for Feign request: {}", requestTemplate.url());
      }
    };
  }

  /**
   * Enable full logging for Feign clients (useful for debugging)
   */
  @Bean
  Logger.Level feignLoggerLevel() {
    return Logger.Level.FULL;
  }
}
