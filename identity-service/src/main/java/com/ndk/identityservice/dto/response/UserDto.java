package com.ndk.identityservice.dto.response;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
  private String id;
  private String username;
  private String email;
  private String displayName;
  private String phoneNumber;
  private String status;
  private String avatar;
  private String bio;
  private List<String> roles;
  private Instant createdAt;
  private Instant updatedAt;
}
