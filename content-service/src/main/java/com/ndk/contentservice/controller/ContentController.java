package com.ndk.contentservice.controller;

import com.ndk.common.api.logging.LogExecutionTime;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.contentservice.dto.request.ContentSearchRequest;
import com.ndk.contentservice.dto.request.CreateContentRequest;
import com.ndk.contentservice.dto.request.UpdateContentRequest;
import com.ndk.contentservice.dto.response.ContentDto;
import com.ndk.contentservice.dto.response.ContentSummaryDto;
import com.ndk.contentservice.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@LogExecutionTime
@RestController
@RequestMapping("/contents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Content Management", description = "APIs for managing contents")
public class ContentController {

  private final ContentService contentService;

  @PostMapping
  @Operation(summary = "Create new content", description = "Create a new content as a creator")
  public ResponseEntity<ApiResponse<ContentDto>> createContent(
      @Valid @RequestBody CreateContentRequest request,
      Authentication authentication) {
    Long userId = getUserIdFromAuth(authentication);
    ContentDto content = contentService.createContent(request, userId);
    return ResponseEntity.ok(ApiResponse.success(content));
  }

  @PutMapping("/{contentId}")
  @Operation(summary = "Update content", description = "Update an existing content")
  public ResponseEntity<ApiResponse<ContentDto>> updateContent(
      @PathVariable Long contentId,
      @Valid @RequestBody UpdateContentRequest request,
      Authentication authentication) {
    Long userId = getUserIdFromAuth(authentication);
    ContentDto content = contentService.updateContent(contentId, request, userId);
    return ResponseEntity.ok(ApiResponse.success(content));
  }

  @GetMapping("/{contentId}")
  @Operation(summary = "Get content by ID", description = "Get detailed content information")
  public ResponseEntity<ApiResponse<ContentDto>> getContentById(
      @PathVariable Long contentId,
      Authentication authentication) {
    Long userId = getUserIdFromAuth(authentication);
    ContentDto content = contentService.getContentById(contentId, userId);
    return ResponseEntity.ok(ApiResponse.success(content));
  }

  @DeleteMapping("/{contentId}")
  @Operation(summary = "Delete content", description = "Delete a content (creator only)")
  public ResponseEntity<ApiResponse<Void>> deleteContent(
      @PathVariable Long contentId,
      Authentication authentication) {
    Long userId = getUserIdFromAuth(authentication);
    contentService.deleteContent(contentId, userId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @PostMapping("/search")
  @Operation(summary = "Search contents", description = "Search and filter contents with pagination")
  public ResponseEntity<ApiResponse<Page<ContentSummaryDto>>> searchContents(
      @RequestBody ContentSearchRequest request) {
    Page<ContentSummaryDto> contents = contentService.searchContents(request);
    return ResponseEntity.ok(ApiResponse.success(contents));
  }

  @PostMapping("/published/search")
  @Operation(summary = "Search published contents", description = "Search published contents available for purchase")
  public ResponseEntity<ApiResponse<Page<ContentSummaryDto>>> searchPublishedContents(
      @RequestBody ContentSearchRequest request) {
    Page<ContentSummaryDto> contents = contentService.getPublishedContents(request);
    return ResponseEntity.ok(ApiResponse.success(contents));
  }

  @PostMapping("/creator/{creatorId}/search")
  @Operation(summary = "Get contents by creator", description = "Get all contents created by a specific user")
  public ResponseEntity<ApiResponse<Page<ContentSummaryDto>>> getContentsByCreator(
      @PathVariable Long creatorId,
      @RequestBody ContentSearchRequest request) {
    Page<ContentSummaryDto> contents = contentService.getContentsByCreator(creatorId, request);
    return ResponseEntity.ok(ApiResponse.success(contents));
  }

  @PostMapping("/category/{categoryId}/search")
  @Operation(summary = "Get contents by category", description = "Get published contents in a specific category")
  public ResponseEntity<ApiResponse<Page<ContentSummaryDto>>> getContentsByCategory(
      @PathVariable Long categoryId,
      @RequestBody ContentSearchRequest request) {
    Page<ContentSummaryDto> contents = contentService.getContentsByCategory(categoryId, request);
    return ResponseEntity.ok(ApiResponse.success(contents));
  }

  @PostMapping("/{contentId}/publish")
  @Operation(summary = "Publish content", description = "Publish a content to make it available for purchase")
  public ResponseEntity<ApiResponse<ContentDto>> publishContent(
      @PathVariable Long contentId,
      Authentication authentication) {
    Long userId = getUserIdFromAuth(authentication);
    ContentDto content = contentService.publishContent(contentId, userId);
    return ResponseEntity.ok(ApiResponse.success(content));
  }

  @PostMapping("/{contentId}/archive")
  @Operation(summary = "Archive content", description = "Archive a content to make it unavailable")
  public ResponseEntity<ApiResponse<ContentDto>> archiveContent(
      @PathVariable Long contentId,
      Authentication authentication) {
    Long userId = getUserIdFromAuth(authentication);
    ContentDto content = contentService.archiveContent(contentId, userId);
    return ResponseEntity.ok(ApiResponse.success(content));
  }

  @PostMapping("/{contentId}/submit-review")
  @Operation(summary = "Submit for review", description = "Submit content for moderator review")
  public ResponseEntity<ApiResponse<ContentDto>> submitForReview(
      @PathVariable Long contentId,
      Authentication authentication) {
    Long userId = getUserIdFromAuth(authentication);
    ContentDto content = contentService.submitForReview(contentId, userId);
    return ResponseEntity.ok(ApiResponse.success(content));
  }

  @PostMapping("/{contentId}/view")
  @Operation(summary = "Increment view count", description = "Track content view (public)")
  public ResponseEntity<ApiResponse<Void>> incrementViewCount(@PathVariable Long contentId) {
    contentService.incrementViewCount(contentId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @PostMapping("/{contentId}/increment-purchase-count")
  @Operation(
      summary = "Increment purchase count", 
      description = "Called by purchase-service after successful purchase (internal API)"
  )
  public ResponseEntity<ApiResponse<Void>> incrementPurchaseCount(@PathVariable Long contentId) {
    contentService.incrementPurchaseCount(contentId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  private Long getUserIdFromAuth(Authentication authentication) {
    Jwt jwt = (Jwt) authentication.getPrincipal();
    return Long.parseLong(jwt.getClaimAsString("userId"));
  }
}
