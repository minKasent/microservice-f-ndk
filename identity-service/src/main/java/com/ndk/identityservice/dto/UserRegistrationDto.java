package com.ndk.identityservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationDto {
  @NotBlank(message = "{user.username.notblank}")
  @Size(min = 3, max = 50, message = "{user.username.size}")
  private String username;
  @NotBlank(message = "{user.email.notblank}")
  @Email(message = "{user.email.invalid}")
  private String email;
  @NotBlank(message = "{user.password.notblank}")
  @Size(min = 6, max = 50, message = "{user.password.size}")
  private String password;
}
