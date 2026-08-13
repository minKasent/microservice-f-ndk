package com.ndk.contentservice.controller;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.contentservice.dto.request.CreateBlockRequest;
import com.ndk.contentservice.dto.request.MoveBlockRequest;
import com.ndk.contentservice.dto.request.UpdateBlockRequest;
import com.ndk.contentservice.dto.response.BlockDto;
import com.ndk.contentservice.service.BlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@RestController
@RequestMapping("/contents/{contentId}/blocks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Content Block Management", description = "APIs for managing content blocks (tree structure)")
public class BlockController {

  private final BlockService blockService;

  @PostMapping
  @Operation(
      summary = "Create new block", 
      description = "Create a new block in a content. Blocks can be nested (parent-child relationship)."
  )
  public ResponseEntity<ApiResponse<BlockDto>> createBlock(
      @PathVariable Long contentId,
      @Valid @RequestBody CreateBlockRequest request,
      Authentication authentication) {
    Long userId = getUserIdFromAuth(authentication);
    BlockDto block = blockService.createBlock(contentId, request, userId);
    return ResponseEntity.ok(ApiResponse.success(block));
  }

  @PutMapping("/{blockId}")
  @Operation(
      summary = "Update block", 
      description = "Update an existing block's content and properties"
  )
  public ResponseEntity<ApiResponse<BlockDto>> updateBlock(
      @PathVariable Long contentId,
      @PathVariable Long blockId,
      @Valid @RequestBody UpdateBlockRequest request,
      Authentication authentication) {
    Long userId = getUserIdFromAuth(authentication);
    BlockDto block = blockService.updateBlock(contentId, blockId, request, userId);
    return ResponseEntity.ok(ApiResponse.success(block));
  }

  @DeleteMapping("/{blockId}")
  @Operation(
      summary = "Delete block", 
      description = "Delete a block and all its children (cascade delete)"
  )
  public ResponseEntity<ApiResponse<Void>> deleteBlock(
      @PathVariable Long contentId,
      @PathVariable Long blockId,
      Authentication authentication) {
    Long userId = getUserIdFromAuth(authentication);
    blockService.deleteBlock(contentId, blockId, userId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  @GetMapping("/{blockId}")
  @Operation(
      summary = "Get block by ID", 
      description = "Get a specific block with its children (tree structure)"
  )
  public ResponseEntity<ApiResponse<BlockDto>> getBlockById(
      @PathVariable Long contentId,
      @PathVariable Long blockId,
      Authentication authentication) {
    Long userId = getUserIdFromAuth(authentication);
    BlockDto block = blockService.getBlockById(contentId, blockId, userId);
    return ResponseEntity.ok(ApiResponse.success(block));
  }

  @GetMapping
  @Operation(
      summary = "Get all blocks", 
      description = "Get all blocks of a content in tree structure (root blocks with nested children)"
  )
  public ResponseEntity<ApiResponse<List<BlockDto>>> getContentBlocks(
      @PathVariable Long contentId,
      Authentication authentication) {
    Long userId = getUserIdFromAuth(authentication);
    List<BlockDto> blocks = blockService.getContentBlocks(contentId, userId);
    return ResponseEntity.ok(ApiResponse.success(blocks));
  }

  @GetMapping("/free")
  @Operation(
      summary = "Get free blocks", 
      description = "Get all free blocks of a content (available for preview without purchase)"
  )
  public ResponseEntity<ApiResponse<List<BlockDto>>> getFreeBlocks(
      @PathVariable Long contentId) {
    List<BlockDto> blocks = blockService.getFreeBlocks(contentId);
    return ResponseEntity.ok(ApiResponse.success(blocks));
  }

  @PutMapping("/{blockId}/move")
  @Operation(
      summary = "Move block", 
      description = "Move a block to a new position and/or parent. Useful for drag-and-drop reordering."
  )
  public ResponseEntity<ApiResponse<BlockDto>> moveBlock(
      @PathVariable Long contentId,
      @PathVariable Long blockId,
      @Valid @RequestBody MoveBlockRequest request,
      Authentication authentication) {
    Long userId = getUserIdFromAuth(authentication);
    BlockDto block = blockService.moveBlock(contentId, blockId, request, userId);
    return ResponseEntity.ok(ApiResponse.success(block));
  }

  @PutMapping("/reorder")
  @Operation(
      summary = "Reorder blocks", 
      description = "Reorder multiple blocks within the same parent. Provide ordered list of block IDs."
  )
  public ResponseEntity<ApiResponse<Void>> reorderBlocks(
      @PathVariable Long contentId,
      @Valid @RequestBody ReorderBlocksRequest request,
      Authentication authentication) {
    Long userId = getUserIdFromAuth(authentication);
    blockService.reorderBlocks(contentId, request.getParentBlockId(), request.getBlockIds(), userId);
    return ResponseEntity.ok(ApiResponse.success(null));
  }

  private Long getUserIdFromAuth(Authentication authentication) {
    Jwt jwt = (Jwt) authentication.getPrincipal();
    return Long.parseLong(jwt.getClaimAsString("userId"));
  }

  // Inner DTO for reorder request
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ReorderBlocksRequest {
    private Long parentBlockId; // null for root blocks
    
    @NotNull(message = "Block IDs are required")
    @Size(min = 1, message = "At least one block ID is required")
    private List<Long> blockIds;
  }
}
