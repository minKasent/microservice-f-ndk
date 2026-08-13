package com.ndk.identityservice.config.jwt;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class JwtConfig {

  private final JwtConfigProperties jwtConfigProperties;

  @Bean
  public JWKSource<SecurityContext> jwkSource() throws Exception {
    KeyPair keyPair = loadOrGenerateKeyPair();

    RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
    RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
    RSAKey rsaKey = new RSAKey.Builder(publicKey)
        .privateKey(privateKey)
        .keyID(jwtConfigProperties.getKeyId())
        .build();
    JWKSet jwkSet = new JWKSet(rsaKey);
    return new ImmutableJWKSet<>(jwkSet);
  }

  private KeyPair loadOrGenerateKeyPair() throws Exception {
    if (jwtConfigProperties.getAutoGenKey()) {
      return generateRsaKey();
    }
    return loadOrGenerateKeyPair(jwtConfigProperties.getPublicKeyPath(),
        jwtConfigProperties.getPrivateKeyPath());
  }

  private KeyPair generateRsaKey() {
    KeyPair keyPair;
    try {
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
      keyPairGenerator.initialize(2048);
      keyPair = keyPairGenerator.generateKeyPair();
    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
    return keyPair;
  }

  private KeyPair loadOrGenerateKeyPair(String publicKeyPath, String privateKeyPath)
      throws Exception {
    PrivateKey privateKey = loadPrivateKey(privateKeyPath);
    PublicKey publicKey = loadPublicKey(publicKeyPath);

    return new KeyPair(publicKey, privateKey);
  }

  private PrivateKey loadPrivateKey(String filePath) throws Exception {
    String pemContent = Files.readString(Paths.get(filePath));

    // Remove PEM headers and whitespace
    String keyContent = pemContent
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
        .replaceAll("\\s", "");

    // Decode Base64 and create key
    byte[] keyBytes = Base64.getDecoder().decode(keyContent);
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");

    return keyFactory.generatePrivate(keySpec);
  }



  private PublicKey loadPublicKey(String filePath) throws Exception {
    String pemContent = Files.readString(Paths.get(filePath));

    // Remove PEM headers and whitespace
    String keyContent = pemContent
        .replace("-----BEGIN PUBLIC KEY-----", "")
        .replace("-----END PUBLIC KEY-----", "")
        .replaceAll("\\s", "");

    // Decode Base64 and create key
    byte[] keyBytes = Base64.getDecoder().decode(keyContent);
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
    KeyFactory keyFactory = KeyFactory.getInstance("RSA");

    return keyFactory.generatePublic(keySpec);
  }
}

