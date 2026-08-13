package com.ndk.notificationservice.config;

import com.ndk.notificationservice.config.properties.BasicCredentialsProperties;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final BasicCredentialsProperties basicCredentialsProperties;

  @Bean
  @Order(2)
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.cors(c -> c.configurationSource(request -> {
      CorsConfiguration corsConfiguration = new CorsConfiguration();
      corsConfiguration.addAllowedOrigin("*");
      corsConfiguration.addAllowedHeader("*");
      corsConfiguration.addAllowedMethod("*");
      return corsConfiguration;
    }));

    http.csrf(AbstractHttpConfigurer::disable);

    http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
        .requestMatchers(
            "/swagger-ui",
            "/swagger-ui/",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/actuator/**"
        ).permitAll()
        .anyRequest().authenticated()
    );

    http.oauth2ResourceServer(configurer -> configurer
        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

    return http.build();
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter =
        new JwtGrantedAuthoritiesConverter();
    grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
    grantedAuthoritiesConverter.setAuthorityPrefix("");

    JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
    authenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
    authenticationConverter.setPrincipalClaimName("userId");
    return authenticationConverter;
  }

  @Bean
  @Order(1)
  public SecurityFilterChain securityFilterChainBasicAuth(HttpSecurity http) throws Exception {
    http.securityMatcher("/internal/**");
    http.csrf(AbstractHttpConfigurer::disable);
    http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
        .anyRequest().authenticated()
    );
    http.httpBasic(Customizer.withDefaults())
        .authenticationManager(authentication -> {
          String username = authentication.getPrincipal().toString();
          String password = authentication.getCredentials().toString();
          String expectedUsername = basicCredentialsProperties.getUsername();
          String expectedPassword = basicCredentialsProperties.getPassword();

          if (expectedUsername != null
              && expectedPassword != null
              && expectedUsername.equals(username)
              && expectedPassword.equals(password)) {
            return UsernamePasswordAuthenticationToken.authenticated(
                username, password, Collections.emptyList());
          }

          throw new org.springframework.security.authentication.BadCredentialsException(
              "Invalid basic credentials");
        });

    return http.build();
  }

}
