package com.ndk.contentservice.service.impl;

import com.ndk.common.api.exception.DevSharingException;
import com.ndk.common.api.response.ApiResponse;
import com.ndk.contentservice.client.PurchaseServiceClient;
import com.ndk.contentservice.dto.request.CreateBlockRequest;
import com.ndk.contentservice.dto.request.MoveBlockRequest;
import com.ndk.contentservice.dto.request.UpdateBlockRequest;
import com.ndk.contentservice.dto.response.BlockDto;
import com.ndk.contentservice.entity.Content;
import com.ndk.contentservice.entity.ContentBlock;
import com.ndk.contentservice.exception.ExceptionEnum;
import com.ndk.contentservice.mapper.BlockMapper;
import com.ndk.contentservice.repository.ContentBlockRepository;
import com.ndk.contentservice.repository.ContentRepository;
import com.ndk.contentservice.service.BlockService;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlockServiceImpl implements BlockService {

  private final ContentBlockRepository blockRepository;
  private final ContentRepository contentRepository;
  private final BlockMapper blockMapper;
  private final PurchaseServiceClient purchaseServiceClient;

  @Override
  @Transactional
  public BlockDto createBlock(Long contentId, CreateBlockRequest request, Long userId) {
    log.info("Creating block for content {} by user {}", contentId, userId);

    // Verify content exists and user is the creator
    Content content = getContentAndVerifyOwnership(contentId, userId);

    // Verify parent block if specified
    ContentBlock parentBlock = null;
    if (request.getParentBlockId() != null) {
      parentBlock = blockRepository.findById(request.getParentBlockId())
          .orElseThrow(() -> new DevSharingException(ExceptionEnum.PARENT_BLOCK_NOT_FOUND, null));
      
      // Verify parent block belongs to the same content
      if (!parentBlock.getContent().getId().equals(contentId)) {
        throw new DevSharingException(ExceptionEnum.PARENT_BLOCK_NOT_IN_SAME_CONTENT, null);
      }
    }

    // Validate position
    if (request.getPosition() < 1) {
      throw new DevSharingException(ExceptionEnum.INVALID_BLOCK_POSITION, null);
    }

    // Adjust positions of existing blocks at and after the new position
    adjustPositionsForInsertion(contentId, request.getParentBlockId(), request.getPosition());

    // Create new block
    Date now = new Date();
    ContentBlock block = ContentBlock.builder()
        .content(content)
        .parentBlock(parentBlock)
        .type(request.getType())
        .textContent(request.getTextContent())
        .properties(request.getProperties())
        .position(request.getPosition())
        .isFree(request.getIsFree() != null ? request.getIsFree() : false)
        .createdAt(now)
        .updatedAt(now)
        .build();

    block = blockRepository.save(block);
    log.info("Block created successfully with id: {}", block.getId());

    return blockMapper.toDto(block);
  }

  @Override
  @Transactional
  public BlockDto updateBlock(Long contentId, Long blockId, UpdateBlockRequest request, Long userId) {
    log.info("Updating block {} in content {} by user {}", blockId, contentId, userId);

    // Verify content exists and user is the creator
    getContentAndVerifyOwnership(contentId, userId);

    // Get block and verify it belongs to the content
    ContentBlock block = getBlockAndVerifyContent(blockId, contentId);

    // Update fields if provided
    if (request.getType() != null) {
      block.setType(request.getType());
    }
    if (request.getTextContent() != null) {
      block.setTextContent(request.getTextContent());
    }
    if (request.getProperties() != null) {
      block.setProperties(request.getProperties());
    }
    if (request.getIsFree() != null) {
      block.setIsFree(request.getIsFree());
    }

    block.setUpdatedAt(new Date());
    block = blockRepository.save(block);

    log.info("Block {} updated successfully", blockId);
    return blockMapper.toDto(block);
  }

  @Override
  @Transactional
  public void deleteBlock(Long contentId, Long blockId, Long userId) {
    log.info("Deleting block {} from content {} by user {}", blockId, contentId, userId);

    // Verify content exists and user is the creator
    getContentAndVerifyOwnership(contentId, userId);

    // Get block and verify it belongs to the content
    ContentBlock block = getBlockAndVerifyContent(blockId, contentId);

    // Get the position and parent of the block being deleted
    Integer deletedPosition = block.getPosition();
    Long parentBlockId = block.getParentBlock() != null ? block.getParentBlock().getId() : null;

    // Delete the block (cascade will delete children)
    blockRepository.delete(block);

    // Adjust positions of remaining blocks after the deleted position
    adjustPositionsAfterDeletion(contentId, parentBlockId, deletedPosition);

    log.info("Block {} deleted successfully", blockId);
  }

  @Override
  @Transactional(readOnly = true)
  public BlockDto getBlockById(Long contentId, Long blockId, Long userId) {
    log.info("Getting block {} from content {}", blockId, contentId);

    // Verify content exists and user has access
    Content content = contentRepository.findById(contentId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.CONTENT_NOT_FOUND, null));

    // Get block and verify it belongs to the content
    ContentBlock block = getBlockAndVerifyContent(blockId, contentId);

    // Check access: creator can see all, others can only see free blocks or if they purchased
    if (!content.getCreatorId().equals(userId) && !block.getIsFree()) {
      // Check if user has purchased the content
      boolean hasPurchased = checkUserOwnership(contentId);
      if (!hasPurchased) {
        throw new DevSharingException(ExceptionEnum.UNAUTHORIZED_CONTENT_ACCESS, null);
      }
    }

    return blockMapper.toDto(block);
  }

  @Override
  @Transactional(readOnly = true)
  public List<BlockDto> getContentBlocks(Long contentId, Long userId) {
    log.info("Getting all blocks for content {} by user {}", contentId, userId);

    // Verify content exists
    Content content = contentRepository.findById(contentId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.CONTENT_NOT_FOUND, null));

    // Get root blocks (blocks without parent)
    List<ContentBlock> rootBlocks = blockRepository
        .findByContentIdAndParentBlockIsNullOrderByPositionAsc(contentId);

    // Check access: creator can see all, others can only see free blocks or if they purchased
    if (!content.getCreatorId().equals(userId)) {
      // Check if user has purchased the content
      boolean hasPurchased = checkUserOwnership(contentId);
      if (!hasPurchased) {
        // User hasn't purchased, only show free blocks
        rootBlocks = rootBlocks.stream()
            .filter(ContentBlock::getIsFree)
            .toList();
      }
      // If purchased, show all blocks (no filtering needed)
    }

    return rootBlocks.stream()
        .map(blockMapper::toDto)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<BlockDto> getFreeBlocks(Long contentId) {
    log.info("Getting free blocks for content {}", contentId);

    // Verify content exists
    contentRepository.findById(contentId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.CONTENT_NOT_FOUND, null));

    List<ContentBlock> freeBlocks = blockRepository.findByContentIdAndIsFreeTrue(contentId);

    return freeBlocks.stream()
        .map(blockMapper::toDto)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public BlockDto moveBlock(Long contentId, Long blockId, MoveBlockRequest request, Long userId) {
    log.info("Moving block {} in content {} to new position {} and parent {}", 
        blockId, contentId, request.getNewPosition(), request.getNewParentBlockId());

    // Verify content exists and user is the creator
    getContentAndVerifyOwnership(contentId, userId);

    // Get block and verify it belongs to the content
    ContentBlock block = getBlockAndVerifyContent(blockId, contentId);

    // Store old position and parent
    Integer oldPosition = block.getPosition();
    Long oldParentId = block.getParentBlock() != null ? block.getParentBlock().getId() : null;
    Long newParentId = request.getNewParentBlockId();

    // Verify new parent if specified
    ContentBlock newParentBlock = null;
    if (newParentId != null) {
      newParentBlock = blockRepository.findById(newParentId)
          .orElseThrow(() -> new DevSharingException(ExceptionEnum.PARENT_BLOCK_NOT_FOUND, null));
      
      // Verify parent block belongs to the same content
      if (!newParentBlock.getContent().getId().equals(contentId)) {
        throw new DevSharingException(ExceptionEnum.PARENT_BLOCK_NOT_IN_SAME_CONTENT, null);
      }

      // Check for circular reference (block cannot be its own parent or descendant)
      if (isDescendant(newParentBlock, blockId)) {
        throw new DevSharingException(ExceptionEnum.CIRCULAR_PARENT_REFERENCE, null);
      }
    }

    // Validate new position
    if (request.getNewPosition() < 1) {
      throw new DevSharingException(ExceptionEnum.INVALID_BLOCK_POSITION, null);
    }

    // If moving within the same parent
    if ((oldParentId == null && newParentId == null) || 
        (oldParentId != null && oldParentId.equals(newParentId))) {
      
      if (!oldPosition.equals(request.getNewPosition())) {
        // Adjust positions for move within same parent
        adjustPositionsForMoveWithinParent(contentId, oldParentId, oldPosition, request.getNewPosition());
        block.setPosition(request.getNewPosition());
      }
    } else {
      // Moving to a different parent
      // Remove from old parent (adjust positions after old position)
      adjustPositionsAfterDeletion(contentId, oldParentId, oldPosition);
      
      // Insert into new parent (adjust positions at and after new position)
      adjustPositionsForInsertion(contentId, newParentId, request.getNewPosition());
      
      block.setParentBlock(newParentBlock);
      block.setPosition(request.getNewPosition());
    }

    block.setUpdatedAt(new Date());
    block = blockRepository.save(block);

    log.info("Block {} moved successfully", blockId);
    return blockMapper.toDto(block);
  }

  @Override
  @Transactional
  public void reorderBlocks(Long contentId, Long parentBlockId, List<Long> blockIds, Long userId) {
    log.info("Reordering {} blocks in content {} under parent {}", 
        blockIds.size(), contentId, parentBlockId);

    // Verify content exists and user is the creator
    getContentAndVerifyOwnership(contentId, userId);

    // Verify all blocks exist and belong to the same parent
    List<ContentBlock> blocks = blockIds.stream()
        .map(id -> {
          ContentBlock block = getBlockAndVerifyContent(id, contentId);
          Long blockParentId = block.getParentBlock() != null ? block.getParentBlock().getId() : null;
          
          // Verify block belongs to the specified parent
          if ((parentBlockId == null && blockParentId != null) || 
              (parentBlockId != null && !parentBlockId.equals(blockParentId))) {
            throw new DevSharingException(ExceptionEnum.BLOCK_NOT_BELONG_TO_CONTENT, null);
          }
          
          return block;
        })
        .collect(Collectors.toList());

    // Update positions
    for (int i = 0; i < blocks.size(); i++) {
      ContentBlock block = blocks.get(i);
      block.setPosition(i + 1);
      block.setUpdatedAt(new Date());
    }

    blockRepository.saveAll(blocks);
    log.info("Blocks reordered successfully");
  }

  // ==================== Private Helper Methods ====================

  /**
   * Get content and verify user is the creator
   */
  private Content getContentAndVerifyOwnership(Long contentId, Long userId) {
    Content content = contentRepository.findById(contentId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.CONTENT_NOT_FOUND, null));

    if (!content.getCreatorId().equals(userId)) {
      throw new DevSharingException(ExceptionEnum.UNAUTHORIZED, null);
    }

    return content;
  }

  /**
   * Get block and verify it belongs to the specified content
   */
  private ContentBlock getBlockAndVerifyContent(Long blockId, Long contentId) {
    ContentBlock block = blockRepository.findById(blockId)
        .orElseThrow(() -> new DevSharingException(ExceptionEnum.BLOCK_NOT_FOUND, null));

    if (!block.getContent().getId().equals(contentId)) {
      throw new DevSharingException(ExceptionEnum.BLOCK_NOT_BELONG_TO_CONTENT, null);
    }

    return block;
  }

  /**
   * Check if a block is a descendant of another block
   */
  private boolean isDescendant(ContentBlock potentialAncestor, Long blockId) {
    if (potentialAncestor.getId().equals(blockId)) {
      return true;
    }

    Set<Long> visited = new HashSet<>();
    return isDescendantRecursive(potentialAncestor, blockId, visited);
  }

  private boolean isDescendantRecursive(ContentBlock current, Long targetBlockId, Set<Long> visited) {
    if (visited.contains(current.getId())) {
      return false; // Avoid infinite loop
    }
    visited.add(current.getId());

    if (current.getId().equals(targetBlockId)) {
      return true;
    }

    for (ContentBlock child : current.getChildren()) {
      if (isDescendantRecursive(child, targetBlockId, visited)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Adjust positions when inserting a new block
   * Increment positions of blocks at and after the insertion position
   */
  private void adjustPositionsForInsertion(Long contentId, Long parentBlockId, Integer insertPosition) {
    List<ContentBlock> blocksToAdjust;
    
    if (parentBlockId == null) {
      blocksToAdjust = blockRepository.findByContentIdAndParentBlockIsNullOrderByPositionAsc(contentId);
    } else {
      blocksToAdjust = blockRepository.findByParentBlockIdOrderByPositionAsc(parentBlockId);
    }

    blocksToAdjust.stream()
        .filter(b -> b.getPosition() >= insertPosition)
        .forEach(b -> {
          b.setPosition(b.getPosition() + 1);
          b.setUpdatedAt(new Date());
        });

    if (!blocksToAdjust.isEmpty()) {
      blockRepository.saveAll(blocksToAdjust);
    }
  }

  /**
   * Adjust positions after deleting a block
   * Decrement positions of blocks after the deleted position
   */
  private void adjustPositionsAfterDeletion(Long contentId, Long parentBlockId, Integer deletedPosition) {
    List<ContentBlock> blocksToAdjust;
    
    if (parentBlockId == null) {
      blocksToAdjust = blockRepository.findByContentIdAndParentBlockIsNullOrderByPositionAsc(contentId);
    } else {
      blocksToAdjust = blockRepository.findByParentBlockIdOrderByPositionAsc(parentBlockId);
    }

    blocksToAdjust.stream()
        .filter(b -> b.getPosition() > deletedPosition)
        .forEach(b -> {
          b.setPosition(b.getPosition() - 1);
          b.setUpdatedAt(new Date());
        });

    if (!blocksToAdjust.isEmpty()) {
      blockRepository.saveAll(blocksToAdjust);
    }
  }

  /**
   * Adjust positions when moving a block within the same parent
   */
  private void adjustPositionsForMoveWithinParent(Long contentId, Long parentBlockId, 
                                                   Integer oldPosition, Integer newPosition) {
    List<ContentBlock> blocksToAdjust;
    
    if (parentBlockId == null) {
      blocksToAdjust = blockRepository.findByContentIdAndParentBlockIsNullOrderByPositionAsc(contentId);
    } else {
      blocksToAdjust = blockRepository.findByParentBlockIdOrderByPositionAsc(parentBlockId);
    }

    if (oldPosition < newPosition) {
      // Moving down: decrement positions between old and new
      blocksToAdjust.stream()
          .filter(b -> b.getPosition() > oldPosition && b.getPosition() <= newPosition)
          .forEach(b -> {
            b.setPosition(b.getPosition() - 1);
            b.setUpdatedAt(new Date());
          });
    } else {
      // Moving up: increment positions between new and old
      blocksToAdjust.stream()
          .filter(b -> b.getPosition() >= newPosition && b.getPosition() < oldPosition)
          .forEach(b -> {
            b.setPosition(b.getPosition() + 1);
            b.setUpdatedAt(new Date());
          });
    }

    if (!blocksToAdjust.isEmpty()) {
      blockRepository.saveAll(blocksToAdjust);
    }
  }

  /**
   * Check if current user owns the content (has purchased it)
   * Calls purchase-service to verify ownership
   */
  private boolean checkUserOwnership(Long contentId) {
    try {
      ApiResponse<Boolean> response = purchaseServiceClient.checkOwnership(contentId);
      return response.getData() != null && response.getData();
    } catch (Exception e) {
      log.error("Error checking content ownership for contentId: {}", contentId, e);
      // If purchase-service is down or error occurs, deny access for safety
      return false;
    }
  }
}
