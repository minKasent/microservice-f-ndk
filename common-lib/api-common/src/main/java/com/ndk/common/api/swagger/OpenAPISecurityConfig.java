package com.ndk.common.api.swagger;

import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class OpenAPISecurityConfig {

  private final OpenAPISecurityProperties openAPISecurityProperties;

  @Bean
  @Order(Ordered.HIGHEST_PRECEDENCE)
  @ConditionalOnProperty(
      value = "api-docs.security.enabled",
      havingValue = "true",
      matchIfMissing = false
  )
  public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
    http.securityMatcher(
            "/v3/api-docs/**"
        ).cors(
            AbstractHttpConfigurer::disable
        ).authorizeHttpRequests(
            authorize -> authorize.anyRequest().authenticated()
        )
        .httpBasic(Customizer.withDefaults())
        .authenticationManager(authentication -> {
          var usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) authentication;
          var username = usernamePasswordAuthenticationToken.getPrincipal();
          var password = usernamePasswordAuthenticationToken.getCredentials().toString();
          if (openAPISecurityProperties.getUsername().equals(username) &&
              password.equals(openAPISecurityProperties.getPassword())) {
            return new UsernamePasswordAuthenticationToken(username, password,
                Collections.singletonList(new SimpleGrantedAuthority("USER")));
          }
          return authentication;
        });
    return http.build();
  }
}
