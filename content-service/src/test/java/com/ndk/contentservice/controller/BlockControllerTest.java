package com.ndk.contentservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.contentservice.dto.request.CreateBlockRequest;
import com.ndk.contentservice.dto.request.MoveBlockRequest;
import com.ndk.contentservice.dto.request.UpdateBlockRequest;
import com.ndk.contentservice.dto.response.BlockDto;
import com.ndk.contentservice.service.BlockService;
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
class BlockControllerTest {

  @Mock
  private BlockService blockService;

  @InjectMocks
  private BlockController blockController;

  private Authentication createMockAuth(String userId) {
    Authentication auth = mock(Authentication.class);
    Jwt jwt = mock(Jwt.class);
    when(auth.getPrincipal()).thenReturn(jwt);
    when(jwt.getClaimAsString("userId")).thenReturn(userId);
    return auth;
  }

  @Test
  @DisplayName("createBlock should return 200 with created block")
  void createBlock_Success() {
    CreateBlockRequest request = CreateBlockRequest.builder().build();
    BlockDto dto = BlockDto.builder().id(101L).build();
    Authentication auth = createMockAuth("10");

    when(blockService.createBlock(1L, request, 10L)).thenReturn(dto);

    ResponseEntity<ApiResponse<BlockDto>> response = blockController.createBlock(1L, request, auth);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().getId()).isEqualTo(101L);
  }

  @Test
  @DisplayName("updateBlock should return 200 with updated block")
  void updateBlock_Success() {
    UpdateBlockRequest request = UpdateBlockRequest.builder().build();
    BlockDto dto = BlockDto.builder().id(101L).build();
    Authentication auth = createMockAuth("10");

    when(blockService.updateBlock(1L, 101L, request, 10L)).thenReturn(dto);

    ResponseEntity<ApiResponse<BlockDto>> response = blockController.updateBlock(1L, 101L, request, auth);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().getId()).isEqualTo(101L);
  }

  @Test
  @DisplayName("deleteBlock should return 200")
  void deleteBlock_Success() {
    Authentication auth = createMockAuth("10");

    ResponseEntity<ApiResponse<Void>> response = blockController.deleteBlock(1L, 101L, auth);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(blockService).deleteBlock(1L, 101L, 10L);
  }

  @Test
  @DisplayName("getBlockById should return 200 with block")
  void getBlockById_Success() {
    BlockDto dto = BlockDto.builder().id(101L).build();
    Authentication auth = createMockAuth("10");

    when(blockService.getBlockById(1L, 101L, 10L)).thenReturn(dto);

    ResponseEntity<ApiResponse<BlockDto>> response = blockController.getBlockById(1L, 101L, auth);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().getId()).isEqualTo(101L);
  }

  @Test
  @DisplayName("getContentBlocks should return 200 with block list")
  void getContentBlocks_Success() {
    List<BlockDto> list = List.of(BlockDto.builder().id(101L).build());
    Authentication auth = createMockAuth("10");

    when(blockService.getContentBlocks(1L, 10L)).thenReturn(list);

    ResponseEntity<ApiResponse<List<BlockDto>>> response = blockController.getContentBlocks(1L, auth);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData()).hasSize(1);
  }

  @Test
  @DisplayName("getFreeBlocks should return 200 with free block list")
  void getFreeBlocks_Success() {
    List<BlockDto> list = List.of(BlockDto.builder().id(101L).build());
    when(blockService.getFreeBlocks(1L)).thenReturn(list);

    ResponseEntity<ApiResponse<List<BlockDto>>> response = blockController.getFreeBlocks(1L);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData()).hasSize(1);
  }

  @Test
  @DisplayName("moveBlock should return 200 with moved block")
  void moveBlock_Success() {
    MoveBlockRequest request = MoveBlockRequest.builder().newPosition(2).build();
    BlockDto dto = BlockDto.builder().id(101L).position(2).build();
    Authentication auth = createMockAuth("10");

    when(blockService.moveBlock(1L, 101L, request, 10L)).thenReturn(dto);

    ResponseEntity<ApiResponse<BlockDto>> response = blockController.moveBlock(1L, 101L, request, auth);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().getData().getPosition()).isEqualTo(2);
  }
}
