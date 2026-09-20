package com.ndk.contentservice.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ndk.common.api.exception.DevSharingException;
import com.ndk.common.api.response.ApiResponse;
import com.ndk.contentservice.client.PurchaseServiceClient;
import com.ndk.contentservice.dto.request.CreateBlockRequest;
import com.ndk.contentservice.dto.request.MoveBlockRequest;
import com.ndk.contentservice.dto.request.UpdateBlockRequest;
import com.ndk.contentservice.dto.response.BlockDto;
import com.ndk.contentservice.entity.BlockType;
import com.ndk.contentservice.entity.Content;
import com.ndk.contentservice.entity.ContentBlock;
import com.ndk.contentservice.exception.ExceptionEnum;
import com.ndk.contentservice.mapper.BlockMapper;
import com.ndk.contentservice.repository.ContentBlockRepository;
import com.ndk.contentservice.repository.ContentRepository;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BlockServiceImplTest {

  @Mock
  private ContentBlockRepository blockRepository;

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private BlockMapper blockMapper;

  @Mock
  private PurchaseServiceClient purchaseServiceClient;

  @InjectMocks
  private BlockServiceImpl blockService;

  @Nested
  @DisplayName("createBlock tests")
  class CreateBlockTests {

    @Test
    @DisplayName("Should create root block successfully")
    void createBlock_Success_RootBlock() {
      Long contentId = 1L;
      Long userId = 10L;
      Content content = Content.builder().id(contentId).creatorId(userId).build();

      CreateBlockRequest request = CreateBlockRequest.builder()
          .type(BlockType.PARAGRAPH)
          .textContent("Introduction to microservices")
          .position(1)
          .isFree(true)
          .build();

      ContentBlock savedBlock = ContentBlock.builder()
          .id(101L)
          .content(content)
          .type(BlockType.PARAGRAPH)
          .position(1)
          .isFree(true)
          .build();

      BlockDto expectedDto = BlockDto.builder().id(101L).position(1).build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(blockRepository.save(any(ContentBlock.class))).thenReturn(savedBlock);
      when(blockMapper.toDto(savedBlock)).thenReturn(expectedDto);

      BlockDto result = blockService.createBlock(contentId, request, userId);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(101L);
      verify(blockRepository).save(any(ContentBlock.class));
    }

    @Test
    @DisplayName("Should throw INVALID_BLOCK_POSITION when position is less than 1")
    void createBlock_ThrowsException_WhenPositionInvalid() {
      Long contentId = 1L;
      Long userId = 10L;
      Content content = Content.builder().id(contentId).creatorId(userId).build();

      CreateBlockRequest request = CreateBlockRequest.builder()
          .type(BlockType.PARAGRAPH)
          .position(0)
          .build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));

      assertThatThrownBy(() -> blockService.createBlock(contentId, request, userId))
          .isInstanceOf(DevSharingException.class)
          .satisfies(e -> {
            DevSharingException ex = (DevSharingException) e;
            assertThat(ex.getExceptionInfo().getErrorCode()).isEqualTo(ExceptionEnum.INVALID_BLOCK_POSITION.getErrorCode());
          });
    }

    @Test
    @DisplayName("Should throw UNAUTHORIZED when user is not the content creator")
    void createBlock_ThrowsException_WhenNotCreator() {
      Long contentId = 1L;
      Content content = Content.builder().id(contentId).creatorId(10L).build();

      CreateBlockRequest request = CreateBlockRequest.builder().position(1).build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));

      assertThatThrownBy(() -> blockService.createBlock(contentId, request, 999L))
          .isInstanceOf(DevSharingException.class)
          .satisfies(e -> {
            DevSharingException ex = (DevSharingException) e;
            assertThat(ex.getExceptionInfo().getErrorCode()).isEqualTo(ExceptionEnum.UNAUTHORIZED.getErrorCode());
          });
    }
  }

  @Nested
  @DisplayName("updateBlock tests")
  class UpdateBlockTests {

    @Test
    @DisplayName("Should update block successfully")
    void updateBlock_Success() {
      Long contentId = 1L;
      Long blockId = 101L;
      Long userId = 10L;

      Content content = Content.builder().id(contentId).creatorId(userId).build();
      ContentBlock block = ContentBlock.builder()
          .id(blockId)
          .content(content)
          .textContent("Old text")
          .type(BlockType.PARAGRAPH)
          .build();

      UpdateBlockRequest request = UpdateBlockRequest.builder()
          .textContent("New updated text")
          .build();

      BlockDto expectedDto = BlockDto.builder().id(blockId).textContent("New updated text").build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(blockRepository.findById(blockId)).thenReturn(Optional.of(block));
      when(blockRepository.save(block)).thenReturn(block);
      when(blockMapper.toDto(block)).thenReturn(expectedDto);

      BlockDto result = blockService.updateBlock(contentId, blockId, request, userId);

      assertThat(result.getTextContent()).isEqualTo("New updated text");
      assertThat(block.getTextContent()).isEqualTo("New updated text");
      verify(blockRepository).save(block);
    }
  }

  @Nested
  @DisplayName("deleteBlock tests")
  class DeleteBlockTests {

    @Test
    @DisplayName("Should delete block successfully")
    void deleteBlock_Success() {
      Long contentId = 1L;
      Long blockId = 101L;
      Long userId = 10L;

      Content content = Content.builder().id(contentId).creatorId(userId).build();
      ContentBlock block = ContentBlock.builder()
          .id(blockId)
          .content(content)
          .position(1)
          .build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(blockRepository.findById(blockId)).thenReturn(Optional.of(block));

      blockService.deleteBlock(contentId, blockId, userId);

      verify(blockRepository).delete(block);
    }
  }

  @Nested
  @DisplayName("getBlockById & permissions tests")
  class GetBlockByIdTests {

    @Test
    @DisplayName("Creator can access paid block")
    void getBlockById_CreatorAccess_Success() {
      Long contentId = 1L;
      Long blockId = 101L;
      Long userId = 10L;

      Content content = Content.builder().id(contentId).creatorId(userId).build();
      ContentBlock block = ContentBlock.builder().id(blockId).content(content).isFree(false).build();
      BlockDto dto = BlockDto.builder().id(blockId).build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(blockRepository.findById(blockId)).thenReturn(Optional.of(block));
      when(blockMapper.toDto(block)).thenReturn(dto);

      BlockDto result = blockService.getBlockById(contentId, blockId, userId);

      assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Non-creator can access free block")
    void getBlockById_NonCreatorFreeBlock_Success() {
      Long contentId = 1L;
      Long blockId = 101L;

      Content content = Content.builder().id(contentId).creatorId(10L).build();
      ContentBlock block = ContentBlock.builder().id(blockId).content(content).isFree(true).build();
      BlockDto dto = BlockDto.builder().id(blockId).build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(blockRepository.findById(blockId)).thenReturn(Optional.of(block));
      when(blockMapper.toDto(block)).thenReturn(dto);

      BlockDto result = blockService.getBlockById(contentId, blockId, 999L);

      assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Non-creator cannot access paid block without purchase")
    void getBlockById_NonCreatorPaidBlock_ThrowsUnauthorized() {
      Long contentId = 1L;
      Long blockId = 101L;

      Content content = Content.builder().id(contentId).creatorId(10L).build();
      ContentBlock block = ContentBlock.builder().id(blockId).content(content).isFree(false).build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(blockRepository.findById(blockId)).thenReturn(Optional.of(block));
      when(purchaseServiceClient.checkOwnership(contentId)).thenReturn(ApiResponse.success(false));

      assertThatThrownBy(() -> blockService.getBlockById(contentId, blockId, 999L))
          .isInstanceOf(DevSharingException.class)
          .satisfies(e -> {
            DevSharingException ex = (DevSharingException) e;
            assertThat(ex.getExceptionInfo().getErrorCode()).isEqualTo(ExceptionEnum.UNAUTHORIZED_CONTENT_ACCESS.getErrorCode());
          });
    }
  }

  @Nested
  @DisplayName("getFreeBlocks tests")
  class GetFreeBlocksTests {

    @Test
    @DisplayName("Should return list of free blocks")
    void getFreeBlocks_Success() {
      Long contentId = 1L;
      Content content = Content.builder().id(contentId).build();
      ContentBlock block1 = ContentBlock.builder().id(101L).isFree(true).build();
      BlockDto dto1 = BlockDto.builder().id(101L).build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(blockRepository.findByContentIdAndIsFreeTrue(contentId)).thenReturn(List.of(block1));
      when(blockMapper.toDto(block1)).thenReturn(dto1);

      List<BlockDto> result = blockService.getFreeBlocks(contentId);

      assertThat(result).hasSize(1);
      assertThat(result.get(0).getId()).isEqualTo(101L);
    }
  }

  @Nested
  @DisplayName("getContentBlocks tests")
  class GetContentBlocksTests {

    @Test
    @DisplayName("Creator can see all blocks")
    void getContentBlocks_Creator_ReturnsAll() {
      Long contentId = 1L;
      Long creatorId = 10L;
      Content content = Content.builder().id(contentId).creatorId(creatorId).build();
      ContentBlock b1 = ContentBlock.builder().id(1L).isFree(true).build();
      ContentBlock b2 = ContentBlock.builder().id(2L).isFree(false).build();
      BlockDto d1 = BlockDto.builder().id(1L).build();
      BlockDto d2 = BlockDto.builder().id(2L).build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(blockRepository.findByContentIdAndParentBlockIsNullOrderByPositionAsc(contentId)).thenReturn(List.of(b1, b2));
      when(blockMapper.toDto(b1)).thenReturn(d1);
      when(blockMapper.toDto(b2)).thenReturn(d2);

      List<BlockDto> result = blockService.getContentBlocks(contentId, creatorId);

      assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Purchased user can see all blocks")
    void getContentBlocks_PurchasedUser_ReturnsAll() {
      Long contentId = 1L;
      Long userId = 99L;
      Content content = Content.builder().id(contentId).creatorId(10L).build();
      ContentBlock b1 = ContentBlock.builder().id(1L).isFree(true).build();
      ContentBlock b2 = ContentBlock.builder().id(2L).isFree(false).build();
      BlockDto d1 = BlockDto.builder().id(1L).build();
      BlockDto d2 = BlockDto.builder().id(2L).build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(purchaseServiceClient.checkOwnership(contentId)).thenReturn(ApiResponse.success(true));
      when(blockRepository.findByContentIdAndParentBlockIsNullOrderByPositionAsc(contentId)).thenReturn(List.of(b1, b2));
      when(blockMapper.toDto(b1)).thenReturn(d1);
      when(blockMapper.toDto(b2)).thenReturn(d2);

      List<BlockDto> result = blockService.getContentBlocks(contentId, userId);

      assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Non-purchased user sees only free blocks")
    void getContentBlocks_NonPurchasedUser_ReturnsOnlyFree() {
      Long contentId = 1L;
      Long userId = 99L;
      Content content = Content.builder().id(contentId).creatorId(10L).build();
      ContentBlock b1 = ContentBlock.builder().id(1L).isFree(true).build();
      ContentBlock b2 = ContentBlock.builder().id(2L).isFree(false).build();
      BlockDto d1 = BlockDto.builder().id(1L).build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(purchaseServiceClient.checkOwnership(contentId)).thenReturn(ApiResponse.success(false));
      when(blockRepository.findByContentIdAndParentBlockIsNullOrderByPositionAsc(contentId)).thenReturn(List.of(b1, b2));
      when(blockMapper.toDto(b1)).thenReturn(d1);

      List<BlockDto> result = blockService.getContentBlocks(contentId, userId);

      assertThat(result).hasSize(1);
    }
  }

  @Nested
  @DisplayName("moveBlock and reorder tests")
  class MoveAndReorderTests {

    @Test
    @DisplayName("moveBlock within same parent should adjust positions and save")
    void moveBlock_WithinSameParent_Success() {
      Long contentId = 1L;
      Long blockId = 101L;
      Long userId = 10L;

      Content content = Content.builder().id(contentId).creatorId(userId).build();
      ContentBlock block = ContentBlock.builder()
          .id(blockId)
          .content(content)
          .position(1)
          .build();

      MoveBlockRequest request = MoveBlockRequest.builder()
          .newPosition(2)
          .newParentBlockId(null)
          .build();

      BlockDto expectedDto = BlockDto.builder().id(blockId).position(2).build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(blockRepository.findById(blockId)).thenReturn(Optional.of(block));
      when(blockRepository.findByContentIdAndParentBlockIsNullOrderByPositionAsc(contentId))
          .thenReturn(List.of(block));
      when(blockRepository.save(block)).thenReturn(block);
      when(blockMapper.toDto(block)).thenReturn(expectedDto);

      BlockDto result = blockService.moveBlock(contentId, blockId, request, userId);

      assertThat(result.getPosition()).isEqualTo(2);
      assertThat(block.getPosition()).isEqualTo(2);
      verify(blockRepository).save(block);
    }

    @Test
    @DisplayName("moveBlock should throw CIRCULAR_PARENT_REFERENCE if new parent is the block itself")
    void moveBlock_CircularReference_ThrowsException() {
      Long contentId = 1L;
      Long blockId = 101L;
      Long userId = 10L;

      Content content = Content.builder().id(contentId).creatorId(userId).build();
      ContentBlock block = ContentBlock.builder()
          .id(blockId)
          .content(content)
          .position(1)
          .build();

      MoveBlockRequest request = MoveBlockRequest.builder()
          .newPosition(1)
          .newParentBlockId(blockId)
          .build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(blockRepository.findById(blockId)).thenReturn(Optional.of(block));

      assertThatThrownBy(() -> blockService.moveBlock(contentId, blockId, request, userId))
          .isInstanceOf(DevSharingException.class)
          .satisfies(e -> {
            DevSharingException ex = (DevSharingException) e;
            assertThat(ex.getExceptionInfo().getErrorCode()).isEqualTo(ExceptionEnum.CIRCULAR_PARENT_REFERENCE.getErrorCode());
          });
    }

    @Test
    @DisplayName("reorderBlocks should update positions of blocks")
    void reorderBlocks_Success() {
      Long contentId = 1L;
      Long userId = 10L;

      Content content = Content.builder().id(contentId).creatorId(userId).build();
      ContentBlock b1 = ContentBlock.builder().id(101L).content(content).position(1).build();
      ContentBlock b2 = ContentBlock.builder().id(102L).content(content).position(2).build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(blockRepository.findById(102L)).thenReturn(Optional.of(b2));
      when(blockRepository.findById(101L)).thenReturn(Optional.of(b1));

      blockService.reorderBlocks(contentId, null, List.of(102L, 101L), userId);

      assertThat(b2.getPosition()).isEqualTo(1);
      assertThat(b1.getPosition()).isEqualTo(2);
      verify(blockRepository).saveAll(any());
    }
  }
}
