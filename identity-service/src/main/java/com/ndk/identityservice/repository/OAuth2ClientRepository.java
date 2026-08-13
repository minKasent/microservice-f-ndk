package com.ndk.identityservice.repository;

import com.ndk.identityservice.entity.OAuth2Client;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuth2ClientRepository extends JpaRepository<OAuth2Client, Long> {
  Optional<OAuth2Client> findByClientId(String clientId);
    
}
