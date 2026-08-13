package com.ndk.identityservice.service.impl;

import com.ndk.common.api.logging.LogExecutionTime;

import static com.ndk.identityservice.exception.ExceptionEnum.USERNAME_EXISTED_ERROR;

import com.ndk.common.api.exception.DevSharingException;
import com.ndk.identityservice.client.CreditServiceClient;
import com.ndk.identityservice.client.NotificationClient;
import com.ndk.identityservice.client.payload.request.SendNotificationRequest;
import com.ndk.identityservice.dto.UserRegistrationDto;
import com.ndk.identityservice.dto.response.UserRegistrationResponseDto;
import com.ndk.identityservice.entity.Role;
import com.ndk.identityservice.entity.RoleEnum;
import com.ndk.identityservice.entity.User;
import com.ndk.identityservice.entity.UserRole;
import com.ndk.identityservice.entity.UserStatus;
import com.ndk.identityservice.entity.UserVerification;
import com.ndk.identityservice.exception.ExceptionEnum;
import com.ndk.identityservice.mapper.UserRegistrationMapper;
import com.ndk.identityservice.model.UserAccountVerificationChannel;
import com.ndk.identityservice.repository.RoleRepository;
import com.ndk.identityservice.repository.UserRepository;
import com.ndk.identityservice.repository.UserRoleRepository;
import com.ndk.identityservice.repository.UserVerificationRepository;
import com.ndk.identityservice.service.UserAccountService;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@LogExecutionTime
@Service
@Slf4j
@RequiredArgsConstructor
public class UserAccountServiceImpl implements UserAccountService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final RoleRepository roleRepository;
  private final UserRoleRepository userRoleRepository;
  private final UserRegistrationMapper userRegistrationMapper;
  private final UserVerificationRepository userVerificationRepository;
  private final NotificationClient notificationClient;
  private final CreditServiceClient creditServiceClient;

  @Override
  @Transactional
  public UserRegistrationResponseDto registerUser(UserRegistrationDto userRegistrationDto) {
    log.info("Registering user: {}", userRegistrationDto.getUsername());

    // default role consumer
    Role consumerRole = getConsumnerRole();
    // check trùng username
    validateUsernameExisted(userRegistrationDto.getUsername());

    // create user and role
    User savedUser = createAndSaveUser(userRegistrationDto);
    assignRoleToUser(savedUser, consumerRole);
    sendEmailVerification(savedUser);

    log.info("Registering user Done: {}", userRegistrationDto.getUsername());

    return userRegistrationMapper.toResponseDto(savedUser);// return responseDto khi đã mapping
  }

  @Override
  @Transactional
  public void verifyEmail(String verificationCode) {
    UserVerification verification = userVerificationRepository.findByVerificationCode(verificationCode)
        .orElseThrow(() -> new DevSharingException(
            ExceptionEnum.USER_VERIFICATION_CODE_NOT_FOUND_ERROR,
            new Object[]{verificationCode}));

    Instant now = Instant.now();

    if (verification.getExpiredAt().isBefore(now)) {
      throw new DevSharingException(ExceptionEnum.USER_VERIFICATION_CODE_EXPIRED_ERROR,
          new Object[]{verificationCode});
    }

    if (verification.getVerifiAt() != null) {
      throw new DevSharingException(ExceptionEnum.USER_VERIFICATION_CODE_EXPIRED_ERROR,
          new Object[]{verificationCode});
    }

    verification.setVerifiAt(now);
    verification.setUpdatedAt(now);
    verification.setUpdatedBy(verification.getUser().getId());
    userVerificationRepository.save(verification);

    User user = verification.getUser();
    user.setStatus(UserStatus.ACTIVE);
    user.setUpdatedAt(now);
    user.setUpdatedBy(user.getId());
    userRepository.save(user);

    createWalletForUser(user);
  }

  /**
   * Create wallet for user in credit-service after email verification.
   */
  private void createWalletForUser(User user) {
    try {
      log.info("Creating wallet for newly verified user: {}", user.getId());
      creditServiceClient.getOrCreateWallet(user.getId().toString());
      log.info("Wallet created successfully for user: {}", user.getId());
    } catch (Exception e) {
      log.error("Failed to create wallet for user: {}. Error: {}", user.getId(), e.getMessage(), e);
      // Don't fail verification if wallet creation fails
    }
  }

  private void sendEmailVerification(User user) {
    try {
      UserVerification userVerification = new UserVerification();
      userVerification.setUser(user);
      userVerification.setChannel(UserAccountVerificationChannel.EMAIL.name());
      userVerification.setReceiver(user.getEmail());
      Instant emailVerificationExpiredAt = Instant.now().plus(1, ChronoUnit.DAYS);
      userVerification.setExpiredAt(emailVerificationExpiredAt);
      userVerification.setVerificationCode(UUID.randomUUID().toString());
      userVerification.setCreatedAt(Instant.now());
      userVerification.setUpdatedAt(Instant.now());
      userVerification.setCreatedBy(0L);
      userVerification.setUpdatedBy(0L);
      userVerificationRepository.save(userVerification);
      String verificationEmailContent = userVerification.getVerificationCode();
      String sendTo = userVerification.getReceiver();
      String sendChannel = userVerification.getChannel();
      SendNotificationRequest request = SendNotificationRequest.builder()
          .to(sendTo)
          .channel(sendChannel)
          .content(verificationEmailContent)
          .build();
      notificationClient.sendNotification(request);
    }catch (Exception e) {
      log.error("Error sending email verification: [{}] | mail: [{}] | msg: [{}] ",
          user.getEmail(),
          user.getUsername(),
          e.getMessage(),
          e);

    }

  }


  private void validateUsernameExisted(String username) throws DevSharingException {
    User userExisted = userRepository.findByUsername(username).orElse(null);
    if (userExisted != null) {
      throw new DevSharingException(USERNAME_EXISTED_ERROR,new Object[] {username});
    }
  }
  private Role getConsumnerRole() {
    return roleRepository
        .findByRoleName(RoleEnum.CONSUMER)
        .orElseThrow(() ->
            new RuntimeException("Could not find consumer role "));
  }
  private User createAndSaveUser(UserRegistrationDto userRegistrationDto) {
    User user = new User();
    user.setUsername(userRegistrationDto.getUsername());
    user.setEmail(userRegistrationDto.getEmail());
    user.setPassword(passwordEncoder.encode(userRegistrationDto.getPassword()));
    user.setCreatedAt(Instant.now());
    user.setUpdatedAt(Instant.now());
    user.setCreatedBy(0L);
    user.setUpdatedBy(0L);
    user.setStatus(UserStatus.PENDING_ACTIVE);
    return userRepository.save(user);
  }
  private void assignRoleToUser(User user, Role role) {
    UserRole userRole = new UserRole();
    userRole.setUser(user);
    userRole.setRole(role);
    userRole.setCreatedAt(Instant.now());
    userRole.setUpdatedAt(Instant.now());
    userRole.setCreatedBy(0L);
    userRole.setUpdatedBy(0L);
    userRoleRepository.save(userRole);
  }
}
