package com.ndk.identityservice.service;

import com.ndk.identityservice.dto.UserRegistrationDto;
import com.ndk.identityservice.dto.response.UserRegistrationResponseDto;

public interface UserAccountService {
  UserRegistrationResponseDto registerUser(UserRegistrationDto userRegistrationDto);

  void verifyEmail(String verificationCode);
}
