package com.ndk.identityservice.config.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JwtConfigProperties {
  private String publicKeyPath;
  private String privateKeyPath;
  private Boolean autoGenKey;
  private String keyId;
}