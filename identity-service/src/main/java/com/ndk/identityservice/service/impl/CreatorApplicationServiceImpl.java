package com.ndk.identityservice.service.impl;

import com.ndk.common.api.logging.LogExecutionTime;

import com.ndk.common.api.exception.DevSharingException;
import com.ndk.identityservice.client.NotificationClient;
import com.ndk.identityservice.client.payload.request.SendNotificationRequest;
import com.ndk.identityservice.dto.request.ApplicationReviewRequestDto;
import com.ndk.identityservice.dto.request.CreatorApplicationRequestDto;
import com.ndk.identityservice.dto.response.CreatorApplicationResponseDto;
import com.ndk.identityservice.entity.ApplicationStatus;
import com.ndk.identityservice.entity.CreatorApplication;
import com.ndk.identityservice.entity.Role;
import com.ndk.identityservice.entity.RoleEnum;
import com.ndk.identityservice.entity.User;
import com.ndk.identityservice.entity.UserRole;
import com.ndk.identityservice.exception.ExceptionEnum;
import com.ndk.identityservice.mapper.CreatorApplicationMapper;
import com.ndk.identityservice.model.UserAccountVerificationChannel;
import com.ndk.identityservice.repository.CreatorApplicationRepository;
import com.ndk.identityservice.repository.RoleRepository;
import com.ndk.identityservice.repository.UserRepository;
import com.ndk.identityservice.repository.UserRoleRepository;
import com.ndk.identityservice.service.CreatorApplicationService;
import jakarta.transaction.Transactional;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@LogExecutionTime
@Service
@Slf4j
@RequiredArgsConstructor
public class CreatorApplicationServiceImpl implements CreatorApplicationService {

  private final CreatorApplicationRepository applicationRepository;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final UserRoleRepository userRoleRepository;
  private final CreatorApplicationMapper applicationMapper;
  private final NotificationClient notificationClient;

  @Override
  @Transactional
  public CreatorApplicationResponseDto submitApplication(Long userId,
      CreatorApplicationRequestDto requestDto) {
    log.info("(submitApplication)User [{}] submitting creator application", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(
            () -> new DevSharingException(ExceptionEnum.USER_NOT_FOUND_ERROR, new Object[]{userId}));

    if (isUserCreator(user)) {
      throw new DevSharingException(ExceptionEnum.USER_ALREADY_CREATOR_ERROR, new Object[]{userId});
    }

    if (applicationRepository.existsByUserAndStatus(user, ApplicationStatus.PENDING)) {
      throw new DevSharingException(ExceptionEnum.PENDING_APPLICATION_EXISTS_ERROR,
          new Object[]{userId});
    }

    CreatorApplication application = CreatorApplication.builder()
        .user(user)
        .reason(requestDto.getReason())
        .portfolioUrl(requestDto.getPortfolioUrl())
        .experienceYears(requestDto.getExperienceYears())
        .specialization(requestDto.getSpecialization())
        .status(ApplicationStatus.PENDING)
        .createdAt(Instant.now())
        .updatedAt(Instant.now())
        .createdBy(userId)
        .updatedBy(userId)
        .build();

    CreatorApplication savedApplication = applicationRepository.save(application);
    sendApplicationSubmittedNotification(user);

    log.info("(submitApplication)Application [{}] created for user [{}]",
        savedApplication.getId(), userId);
    return applicationMapper.toResponseDto(savedApplication);
  }

  @Override
  @Transactional
  public CreatorApplicationResponseDto reviewApplication(
      Long applicationId,
      ApplicationReviewRequestDto reviewRequestDto,
      Long reviewerId) {

    log.info("(reviewApplication)Reviewing application [{}] by moderator [{}]",
        applicationId, reviewerId);

    CreatorApplication application = applicationRepository.findById(applicationId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.APPLICATION_NOT_FOUND_ERROR,
            new Object[]{applicationId}));

    if (application.getStatus() != ApplicationStatus.PENDING) {
      throw new DevSharingException(ExceptionEnum.INVALID_APPLICATION_STATUS_ERROR,
          new Object[]{applicationId, application.getStatus()});
    }

    User reviewer = userRepository.findById(reviewerId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.USER_NOT_FOUND_ERROR,
            new Object[]{reviewerId}));

    if (!isUserModerator(reviewer)) {
      throw new DevSharingException(ExceptionEnum.UNAUTHORIZED_REVIEW_ERROR,
          new Object[]{reviewerId});
    }

    application.setStatus(reviewRequestDto.getStatus());
    application.setReviewedBy(reviewerId);
    application.setReviewedAt(Instant.now());
    application.setReviewNote(reviewRequestDto.getReviewNote());
    application.setUpdatedAt(Instant.now());
    application.setUpdatedBy(reviewerId);

    CreatorApplication updatedApplication = applicationRepository.save(application);

    if (reviewRequestDto.getStatus() == ApplicationStatus.APPROVED) {
      upgradeUserToCreator(application.getUser(), reviewerId);
      sendApplicationApprovedNotification(application.getUser());
    } else if (reviewRequestDto.getStatus() == ApplicationStatus.REJECTED) {
      sendApplicationRejectedNotification(application.getUser(), reviewRequestDto.getReviewNote());
    }

    log.info("(reviewApplication)Application [{}] reviewed with status [{}]",
        applicationId, reviewRequestDto.getStatus());

    return applicationMapper.toResponseDto(updatedApplication);
  }

  @Override
  public Page<CreatorApplicationResponseDto> getPendingApplications(Pageable pageable) {
    log.info("(getPendingApplications)Fetching pending applications");
    return applicationRepository.findPendingApplications(ApplicationStatus.PENDING, pageable)
        .map(applicationMapper::toResponseDto);
  }

  @Override
  public Page<CreatorApplicationResponseDto> getUserApplications(Long userId, Pageable pageable) {
    log.info("(getUserApplications)Fetching applications for user [{}]", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(
            () -> new DevSharingException(ExceptionEnum.USER_NOT_FOUND_ERROR, new Object[]{userId}));

    var applications = applicationRepository.findByUser(user);
    return applications.stream()
        .skip(pageable.getOffset())
        .limit(pageable.getPageSize())
        .map(applicationMapper::toResponseDto)
        .collect(java.util.stream.Collectors.collectingAndThen(
            java.util.stream.Collectors.toList(),
            list -> new PageImpl<>(list, pageable, applications.size())
        ));
  }

  @Override
  public CreatorApplicationResponseDto getApplicationById(Long applicationId) {
    log.info("(getApplicationById)Fetching application [{}]", applicationId);

    CreatorApplication application = applicationRepository.findById(applicationId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.APPLICATION_NOT_FOUND_ERROR,
            new Object[]{applicationId}));

    return applicationMapper.toResponseDto(application);
  }

  private boolean isUserCreator(User user) {
    Role creatorRole = roleRepository.findByRoleName(RoleEnum.CREATOR).orElse(null);
    if (creatorRole == null || user.getUserRoles() == null) {
      return false;
    }

    return user.getUserRoles().stream()
        .anyMatch(userRole -> userRole.getRole().getId().equals(creatorRole.getId()));
  }

  private boolean isUserModerator(User user) {
    Role moderatorRole = roleRepository.findByRoleName(RoleEnum.MODERATOR).orElse(null);
    if (moderatorRole == null || user.getUserRoles() == null) {
      return false;
    }

    return user.getUserRoles().stream()
        .anyMatch(userRole -> userRole.getRole().getId().equals(moderatorRole.getId()));
  }

  @Transactional
  private void upgradeUserToCreator(User user, Long reviewerId) {
    log.info("(upgradeUserToCreator)Upgrading user [{}] to creator role", user.getId());

    Role creatorRole = roleRepository.findByRoleName(RoleEnum.CREATOR)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.ROLE_NOT_FOUND_ERROR,
            new Object[]{RoleEnum.CREATOR}));

    boolean hasCreatorRole = user.getUserRoles() != null && user.getUserRoles().stream()
        .anyMatch(userRole -> userRole.getRole().getId().equals(creatorRole.getId()));

    if (!hasCreatorRole) {
      UserRole userRole = UserRole.builder()
          .user(user)
          .role(creatorRole)
          .createdAt(Instant.now())
          .updatedAt(Instant.now())
          .createdBy(reviewerId)
          .updatedBy(reviewerId)
          .build();

      userRoleRepository.save(userRole);
      log.info("(upgradeUserToCreator)User [{}] successfully upgraded to creator", user.getId());
    }
  }

  private void sendApplicationSubmittedNotification(User user) {
    try {
      SendNotificationRequest request = SendNotificationRequest.builder()
          .channel(UserAccountVerificationChannel.EMAIL.name())
          .to(user.getEmail())
          .content(buildApplicationSubmittedEmail(user))
          .build();

      notificationClient.sendNotification(request);
      log.info("(sendApplicationSubmittedNotification)Notification sent to user [{}]", user.getId());
    } catch (Exception e) {
      log.error("(sendApplicationSubmittedNotification)Failed to send notification to user [{}]: {}",
          user.getId(), e.getMessage(), e);
    }
  }

  private void sendApplicationApprovedNotification(User user) {
    try {
      SendNotificationRequest request = SendNotificationRequest.builder()
          .channel(UserAccountVerificationChannel.EMAIL.name())
          .to(user.getEmail())
          .content(buildApplicationApprovedEmail(user))
          .build();

      notificationClient.sendNotification(request);
      log.info("(sendApplicationApprovedNotification)Notification sent to user [{}]", user.getId());
    } catch (Exception e) {
      log.error("(sendApplicationApprovedNotification)Failed to send notification to user [{}]: {}",
          user.getId(), e.getMessage(), e);
    }
  }

  private void sendApplicationRejectedNotification(User user, String reason) {
    try {
      SendNotificationRequest request = SendNotificationRequest.builder()
          .channel(UserAccountVerificationChannel.EMAIL.name())
          .to(user.getEmail())
          .content(buildApplicationRejectedEmail(user, reason))
          .build();

      notificationClient.sendNotification(request);
      log.info("(sendApplicationRejectedNotification)Notification sent to user [{}]", user.getId());
    } catch (Exception e) {
      log.error("(sendApplicationRejectedNotification)Failed to send notification to user [{}]: {}",
          user.getId(), e.getMessage(), e);
    }
  }

  private String buildApplicationSubmittedEmail(User user) {
    return String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                .content { padding: 20px; background-color: #f9f9f9; }
                .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Don Ung Tuyen Da Duoc Gui</h1>
                </div>
                <div class="content">
                    <p>Xin chao <strong>%s</strong>,</p>
                    <p>Chung toi da nhan duoc don ung tuyen tro thanh Content Creator cua ban.</p>
                    <p>Don cua ban dang duoc xem xet. Chung toi se thong bao ket qua som nhat.</p>
                </div>
                <div class="footer">
                    <p>DevSharing Platform</p>
                </div>
            </div>
        </body>
        </html>
        """, user.getUsername());
  }

  private String buildApplicationApprovedEmail(User user) {
    return String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                .content { padding: 20px; background-color: #f9f9f9; }
                .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Chuc Mung!</h1>
                </div>
                <div class="content">
                    <p>Xin chao <strong>%s</strong>,</p>
                    <p>Don ung tuyen Creator cua ban da duoc PHE DUYET.</p>
                    <p>Ban da chinh thuc tro thanh Content Creator tren nen tang.</p>
                </div>
                <div class="footer">
                    <p>DevSharing Platform</p>
                </div>
            </div>
        </body>
        </html>
        """, user.getUsername());
  }

  private String buildApplicationRejectedEmail(User user, String reason) {
    String reasonText = (reason != null && !reason.isEmpty())
        ? reason
        : "Don cua ban chua dap ung du yeu cau hien tai.";

    return String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background-color: #f44336; color: white; padding: 20px; text-align: center; }
                .content { padding: 20px; background-color: #f9f9f9; }
                .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>Thong Bao Ve Don Ung Tuyen</h1>
                </div>
                <div class="content">
                    <p>Xin chao <strong>%s</strong>,</p>
                    <p>Don ung tuyen cua ban chua duoc chap thuan.</p>
                    <p><strong>Ly do:</strong> %s</p>
                    <p>Ban co the nop don moi sau khi da cai thien ho so.</p>
                </div>
                <div class="footer">
                    <p>DevSharing Platform</p>
                </div>
            </div>
        </body>
        </html>
        """, user.getUsername(), reasonText);
  }
}
