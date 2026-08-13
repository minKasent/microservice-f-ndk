package com.ndk.identityservice.controller;

import com.ndk.common.api.logging.LogExecutionTime;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.identityservice.dto.response.UserDto;
import com.ndk.identityservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@LogExecutionTime
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for user information")
public class UserController {

  private final UserService userService;

  @GetMapping("/{userId}")
  @PreAuthorize("isAuthenticated()")
  @Operation(
      summary = "Get user by ID",
      description = "Get user information by user ID (for internal service calls)"
  )
  public ResponseEntity<ApiResponse<UserDto>> getUserById(@PathVariable String userId) {
    UserDto user = userService.getUserById(userId);
    return ResponseEntity.ok(ApiResponse.success(user));
  }

  @GetMapping("/me")
  @PreAuthorize("isAuthenticated()")
  @Operation(
      summary = "Get current user",
      description = "Get current authenticated user information"
  )
  public ResponseEntity<ApiResponse<UserDto>> getCurrentUser(Authentication authentication) {
    String userId = authentication.getName();
    UserDto user = userService.getCurrentUser(userId);
    return ResponseEntity.ok(ApiResponse.success(user));
  }
}
