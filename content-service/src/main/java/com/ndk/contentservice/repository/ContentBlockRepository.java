package com.ndk.contentservice.repository;

import com.ndk.contentservice.entity.BlockType;
import com.ndk.contentservice.entity.ContentBlock;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentBlockRepository extends JpaRepository<ContentBlock, Long> {
  
  // Get all root blocks (top-level) of a content
  List<ContentBlock> findByContentIdAndParentBlockIsNullOrderByPositionAsc(Long contentId);
  
  // Get all children of a parent block
  List<ContentBlock> findByParentBlockIdOrderByPositionAsc(Long parentBlockId);
  
  // Get all blocks of a content (flat list)
  List<ContentBlock> findByContentIdOrderByPositionAsc(Long contentId);
  
  // Get free blocks for preview
  List<ContentBlock> findByContentIdAndIsFreeTrue(Long contentId);
  
  // Count blocks by type
  long countByContentIdAndType(Long contentId, BlockType type);
  
  // Get blocks by type
  List<ContentBlock> findByContentIdAndType(Long contentId, BlockType type);
  
  // Delete all blocks of a content
  void deleteByContentId(Long contentId);
  
  // Check if position exists in same parent (for non-root blocks)
  @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM ContentBlock b " +
         "WHERE b.content.id = :contentId AND b.parentBlock.id = :parentBlockId AND b.position = :position")
  boolean existsByContentIdAndParentBlockIdAndPosition(
      @Param("contentId") Long contentId, 
      @Param("parentBlockId") Long parentBlockId, 
      @Param("position") Integer position
  );
  
  // Check if position exists for root blocks
  @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM ContentBlock b " +
         "WHERE b.content.id = :contentId AND b.parentBlock IS NULL AND b.position = :position")
  boolean existsByContentIdAndNullParentAndPosition(
      @Param("contentId") Long contentId, 
      @Param("position") Integer position
  );
}
