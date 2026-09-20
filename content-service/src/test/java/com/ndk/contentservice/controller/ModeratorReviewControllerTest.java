package com.ndk.contentservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.contentservice.dto.request.ReviewContentRequest;
import com.ndk.contentservice.dto.response.ContentDto;
import com.ndk.contentservice.dto.response.ContentReviewDto;
import com.ndk.contentservice.entity.ReviewAction;
import com.ndk.contentservice.service.ModeratorReviewService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class ModeratorReviewControllerTest {

  @Mock
  private ModeratorReviewService moderatorReviewService;

  @InjectMocks
  private ModeratorReviewController moderatorReviewController;

  @Test
  @DisplayName("reviewContent should return 200 with reviewed content")
  void reviewContent_Success() {
    Long contentId = 1L;
    ReviewContentRequest request = ReviewContentRequest.builder()
        .action(ReviewAction.APPROVE)
        .feedback("Approved")
        .build();

    Authentication auth = mock(Authentication.class);
    Jwt jwt = mock(Jwt.class);
    when(auth.getPrincipal()).thenReturn(jwt);
    when(jwt.getClaimAsString("userId")).thenReturn("99");

    ContentDto contentDto = ContentDto.builder().id(contentId).build();
    when(moderatorReviewService.reviewContent(contentId, request, 99L)).thenReturn(contentDto);

    ResponseEntity<ApiResponse<ContentDto>> response = moderatorReviewController.reviewContent(contentId, request, auth);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().getId()).isEqualTo(contentId);
  }

  @Test
  @DisplayName("getReviewHistory should return 200 with review history")
  void getReviewHistory_Success() {
    Long contentId = 1L;
    List<ContentReviewDto> list = List.of(ContentReviewDto.builder().id(10L).build());
    when(moderatorReviewService.getContentReviewHistory(contentId)).thenReturn(list);

    ResponseEntity<ApiResponse<List<ContentReviewDto>>> response = moderatorReviewController.getReviewHistory(contentId);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData()).hasSize(1);
  }

  @Test
  @DisplayName("getPendingReviews should return 200 with pending contents")
  void getPendingReviews_Success() {
    List<ContentDto> list = List.of(ContentDto.builder().id(1L).build());
    when(moderatorReviewService.getPendingReviewContents()).thenReturn(list);

    ResponseEntity<ApiResponse<List<ContentDto>>> response = moderatorReviewController.getPendingReviews();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData()).hasSize(1);
  }
}
