package com.ndk.contentservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.contentservice.dto.request.ContentSearchRequest;
import com.ndk.contentservice.dto.request.CreateContentRequest;
import com.ndk.contentservice.dto.request.UpdateContentRequest;
import com.ndk.contentservice.dto.response.ContentDto;
import com.ndk.contentservice.dto.response.ContentSummaryDto;
import com.ndk.contentservice.service.ContentService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

@ExtendWith(MockitoExtension.class)
class ContentControllerTest {

  @Mock
  private ContentService contentService;

  @InjectMocks
  private ContentController contentController;

  private Authentication createMockAuth(String userId) {
    Authentication auth = mock(Authentication.class);
    Jwt jwt = mock(Jwt.class);
    when(auth.getPrincipal()).thenReturn(jwt);
    when(jwt.getClaimAsString("userId")).thenReturn(userId);
    return auth;
  }

  @Test
  @DisplayName("createContent should return 200 with created content")
  void createContent_Success() {
    CreateContentRequest request = CreateContentRequest.builder().title("Title").build();
    ContentDto dto = ContentDto.builder().id(1L).build();
    Authentication auth = createMockAuth("10");

    when(contentService.createContent(request, 10L)).thenReturn(dto);

    ResponseEntity<ApiResponse<ContentDto>> response = contentController.createContent(request, auth);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("updateContent should return 200 with updated content")
  void updateContent_Success() {
    UpdateContentRequest request = UpdateContentRequest.builder().title("New").build();
    ContentDto dto = ContentDto.builder().id(1L).build();
    Authentication auth = createMockAuth("10");

    when(contentService.updateContent(1L, request, 10L)).thenReturn(dto);

    ResponseEntity<ApiResponse<ContentDto>> response = contentController.updateContent(1L, request, auth);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("getContentById should return 200 with content details")
  void getContentById_Success() {
    ContentDto dto = ContentDto.builder().id(1L).build();
    Authentication auth = createMockAuth("10");

    when(contentService.getContentById(1L, 10L)).thenReturn(dto);

    ResponseEntity<ApiResponse<ContentDto>> response = contentController.getContentById(1L, auth);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("deleteContent should return 200")
  void deleteContent_Success() {
    Authentication auth = createMockAuth("10");

    ResponseEntity<ApiResponse<Void>> response = contentController.deleteContent(1L, auth);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(contentService).deleteContent(1L, 10L);
  }

  @Test
  @DisplayName("searchContents should return 200 with paged content")
  void searchContents_Success() {
    ContentSearchRequest request = new ContentSearchRequest();
    Page<ContentSummaryDto> page = new PageImpl<>(List.of(ContentSummaryDto.builder().id(1L).build()));

    when(contentService.searchContents(request)).thenReturn(page);

    ResponseEntity<ApiResponse<Page<ContentSummaryDto>>> response = contentController.searchContents(request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().getContent()).hasSize(1);
  }

  @Test
  @DisplayName("publishContent should return 200 with published content")
  void publishContent_Success() {
    ContentDto dto = ContentDto.builder().id(1L).build();
    Authentication auth = createMockAuth("10");

    when(contentService.publishContent(1L, 10L)).thenReturn(dto);

    ResponseEntity<ApiResponse<ContentDto>> response = contentController.publishContent(1L, auth);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("archiveContent should return 200 with archived content")
  void archiveContent_Success() {
    ContentDto dto = ContentDto.builder().id(1L).build();
    Authentication auth = createMockAuth("10");

    when(contentService.archiveContent(1L, 10L)).thenReturn(dto);

    ResponseEntity<ApiResponse<ContentDto>> response = contentController.archiveContent(1L, auth);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("submitForReview should return 200 with submitted content")
  void submitForReview_Success() {
    ContentDto dto = ContentDto.builder().id(1L).build();
    Authentication auth = createMockAuth("10");

    when(contentService.submitForReview(1L, 10L)).thenReturn(dto);

    ResponseEntity<ApiResponse<ContentDto>> response = contentController.submitForReview(1L, auth);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().getId()).isEqualTo(1L);
  }

  @Test
  @DisplayName("incrementViewCount should return 200")
  void incrementViewCount_Success() {
    ResponseEntity<ApiResponse<Void>> response = contentController.incrementViewCount(1L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(contentService).incrementViewCount(1L);
  }

  @Test
  @DisplayName("incrementPurchaseCount should return 200")
  void incrementPurchaseCount_Success() {
    ResponseEntity<ApiResponse<Void>> response = contentController.incrementPurchaseCount(1L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(contentService).incrementPurchaseCount(1L);
  }
}
