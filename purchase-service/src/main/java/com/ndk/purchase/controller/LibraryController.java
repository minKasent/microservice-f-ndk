package com.ndk.purchase.controller;

import com.ndk.common.api.logging.LogExecutionTime;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.purchase.dto.response.LibraryItemDto;
import com.ndk.purchase.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@LogExecutionTime
@RestController
@RequestMapping("/api/v1/library")
@RequiredArgsConstructor
public class LibraryController {
  private final LibraryService libraryService;
  
  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Page<LibraryItemDto>>> getMyLibrary(
      @AuthenticationPrincipal Jwt jwt,
      Pageable pageable
  ) {
    String userId = jwt.getSubject();
    Page<LibraryItemDto> library = libraryService.getMyLibrary(userId, pageable);
    return ResponseEntity.ok(ApiResponse.success(library));
  }
  
  @GetMapping("/{contentId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<LibraryItemDto>> getLibraryItem(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long contentId
  ) {
    String userId = jwt.getSubject();
    LibraryItemDto item = libraryService.getLibraryItem(userId, contentId);
    return ResponseEntity.ok(ApiResponse.success(item));
  }
  
  @GetMapping("/{contentId}/check")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Boolean>> checkOwnership(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long contentId
  ) {
    String userId = jwt.getSubject();
    boolean owned = libraryService.checkOwnership(userId, contentId);
    return ResponseEntity.ok(ApiResponse.success(owned));
  }
  
  @PostMapping("/{contentId}/access")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Void>> trackAccess(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable Long contentId
  ) {
    String userId = jwt.getSubject();
    libraryService.trackAccess(userId, contentId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }
}
