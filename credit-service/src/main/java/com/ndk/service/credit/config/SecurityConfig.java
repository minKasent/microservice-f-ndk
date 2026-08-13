package com.ndk.service.credit.config;

import com.ndk.service.credit.config.properties.BasicCredentialsProperties;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.core.convert.converter.Converter;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
  private final BasicCredentialsProperties basicCredentialsProperties;

  @Bean
  @Order(1)
  public SecurityFilterChain securityFilterChainBasicAuth(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable);
    http.authorizeHttpRequests(
        authorizeRequests -> authorizeRequests
            .requestMatchers("/swagger-ui/**",
                "/v3/api-docs/**",
                "/actuator/**").permitAll()
            .anyRequest().authenticated()
    );
    http.securityMatcher(
            request -> {
              String  authHeader = request.getHeader("Authorization");
              return authHeader != null && authHeader.startsWith("Basic ");
            }
        ).httpBasic(Customizer.withDefaults())
        .authenticationManager(authentication -> {
          String username = authentication.getPrincipal().toString();
          String password = authentication.getCredentials().toString();
          if (username == null || password == null) {
            return authentication;
          }
          if (basicCredentialsProperties.getUsername().equals(username)
              && basicCredentialsProperties.getPassword().equals(password)) {
            return new UsernamePasswordAuthenticationToken(username, password,
                Collections.emptyList());
          }
          return authentication;
        });
    return http.build();
  }

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

    http.authorizeHttpRequests(
        authorizeRequests -> authorizeRequests
            .requestMatchers("/swagger-ui/**",
                "/v3/api-docs/**",
                "/actuator/**").permitAll()
            .anyRequest().authenticated()
    );

    http.oauth2ResourceServer(
        configurer -> configurer.jwt(
            jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter())
        )
    );
    return http.build();
  }

  private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
    grantedAuthoritiesConverter.setAuthorityPrefix(""); // roles đã ở dạng ROLE_*

    JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
    authenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
    return authenticationConverter;
  }
}
