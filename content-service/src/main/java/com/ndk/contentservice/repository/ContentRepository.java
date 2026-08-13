package com.ndk.contentservice.repository;

import com.ndk.contentservice.entity.Content;
import com.ndk.contentservice.entity.ContentStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long>, JpaSpecificationExecutor<Content> {
  Page<Content> findByStatus(ContentStatus status, Pageable pageable);
  
  List<Content> findByStatusOrderByUpdatedAtAsc(ContentStatus status);
  
  Page<Content> findByCreatorId(Long creatorId, Pageable pageable);
  
  Page<Content> findByCreatorIdAndStatus(Long creatorId, ContentStatus status, Pageable pageable);
  
  Page<Content> findByTitleContainingIgnoreCaseAndStatus(String title, ContentStatus status, Pageable pageable);
  
  @Query("SELECT c FROM Content c WHERE c.status = :status AND c.category.id = :categoryId")
  Page<Content> findByStatusAndCategory(@Param("status") ContentStatus status, 
                                         @Param("categoryId") Long categoryId, 
                                         Pageable pageable);
  
  @Query("SELECT c FROM Content c WHERE c.status = 'PUBLISHED' AND " +
         "(LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
         "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
  Page<Content> searchPublishedContent(@Param("keyword") String keyword, Pageable pageable);
}
