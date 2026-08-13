package com.ndk.contentservice.service;

import com.ndk.contentservice.dto.request.CreateBlockRequest;
import com.ndk.contentservice.dto.request.MoveBlockRequest;
import com.ndk.contentservice.dto.request.UpdateBlockRequest;
import com.ndk.contentservice.dto.response.BlockDto;
import java.util.List;

public interface BlockService {
  
  /**
   * Create a new block in a content
   * @param contentId The content ID
   * @param request The block creation request
   * @param userId The user ID (must be content creator)
   * @return Created block DTO
   */
  BlockDto createBlock(Long contentId, CreateBlockRequest request, Long userId);
  
  /**
   * Update an existing block
   * @param contentId The content ID
   * @param blockId The block ID
   * @param request The block update request
   * @param userId The user ID (must be content creator)
   * @return Updated block DTO
   */
  BlockDto updateBlock(Long contentId, Long blockId, UpdateBlockRequest request, Long userId);
  
  /**
   * Delete a block (and all its children)
   * @param contentId The content ID
   * @param blockId The block ID
   * @param userId The user ID (must be content creator)
   */
  void deleteBlock(Long contentId, Long blockId, Long userId);
  
  /**
   * Get a specific block by ID
   * @param contentId The content ID
   * @param blockId The block ID
   * @param userId The user ID (for access control)
   * @return Block DTO with children
   */
  BlockDto getBlockById(Long contentId, Long blockId, Long userId);
  
  /**
   * Get all blocks of a content in tree structure (root blocks with nested children)
   * @param contentId The content ID
   * @param userId The user ID (for access control)
   * @return List of root blocks with nested children
   */
  List<BlockDto> getContentBlocks(Long contentId, Long userId);
  
  /**
   * Get all free blocks of a content (for preview without purchase)
   * @param contentId The content ID
   * @return List of free blocks
   */
  List<BlockDto> getFreeBlocks(Long contentId);
  
  /**
   * Move a block to a new position and/or parent
   * @param contentId The content ID
   * @param blockId The block ID
   * @param request The move request (new parent and position)
   * @param userId The user ID (must be content creator)
   * @return Updated block DTO
   */
  BlockDto moveBlock(Long contentId, Long blockId, MoveBlockRequest request, Long userId);
  
  /**
   * Reorder blocks within the same parent
   * @param contentId The content ID
   * @param parentBlockId The parent block ID (null for root blocks)
   * @param blockIds Ordered list of block IDs
   * @param userId The user ID (must be content creator)
   */
  void reorderBlocks(Long contentId, Long parentBlockId, List<Long> blockIds, Long userId);
}
