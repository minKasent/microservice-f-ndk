package com.ndk.identityservice.controller;

import com.ndk.common.api.logging.LogExecutionTime;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.identityservice.dto.UserRegistrationDto;
import com.ndk.identityservice.dto.response.UserRegistrationResponseDto;
import com.ndk.identityservice.service.UserAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@LogExecutionTime
@RestController
@RequestMapping("/api/v1/users/account")
@RequiredArgsConstructor
public class UserAccountController {
  private final UserAccountService userAccountService;

  @PostMapping
  public ResponseEntity<ApiResponse<UserRegistrationResponseDto>> registerUser(
      @Valid @RequestBody UserRegistrationDto userRegistrationDto) {
    return ResponseEntity.ok(
        ApiResponse.success(userAccountService.registerUser(userRegistrationDto))
    );
  }

  @GetMapping("/verify")
  public ResponseEntity<ApiResponse<Void>> verifyEmail(
      @RequestParam("code") String verificationCode
  ) {
    userAccountService.verifyEmail(verificationCode);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
