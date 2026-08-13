package com.ndk.identityservice.repository;

import com.ndk.identityservice.entity.UserVerification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserVerificationRepository extends JpaRepository<UserVerification, Long> {

  Optional<UserVerification> findByVerificationCode(String verificationCode);
}
