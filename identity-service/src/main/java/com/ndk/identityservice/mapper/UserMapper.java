package com.ndk.identityservice.mapper;

import com.ndk.identityservice.dto.response.UserDto;
import com.ndk.identityservice.entity.User;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public UserDto toDto(User user) {
    if (user == null) {
      return null;
    }

    return UserDto.builder()
        .id(user.getId().toString())
        .username(user.getUsername())
        .email(user.getEmail())
        .displayName(buildDisplayName(user))
        .phoneNumber(user.getPhoneNumber())
        .status(user.getStatus() != null ? user.getStatus().name() : null)
        .avatar(user.getProfile() != null ? user.getProfile().getAvartar() : null)
        .bio(user.getProfile() != null ? user.getProfile().getBio() : null)
        .roles(user.getUserRoles() != null
            ? user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getRoleName().name())
                .collect(Collectors.toList())
            : null)
        .createdAt(user.getCreatedAt())
        .updatedAt(user.getUpdatedAt())
        .build();
  }

  private String buildDisplayName(User user) {
    String firstname = user.getFirstname() != null ? user.getFirstname() : "";
    String lastname = user.getLastname() != null ? user.getLastname() : "";
    String displayName = (firstname + " " + lastname).trim();
    return displayName.isEmpty() ? user.getUsername() : displayName;
  }
}
