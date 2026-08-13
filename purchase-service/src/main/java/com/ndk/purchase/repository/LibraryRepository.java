package com.ndk.purchase.repository;

import com.ndk.purchase.entity.Library;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryRepository extends JpaRepository<Library, Long> {
  Optional<Library> findByUserIdAndContentId(String userId, Long contentId);
  
  Page<Library> findByUserId(String userId, Pageable pageable);
  
  Page<Library> findByUserIdAndIsActive(String userId, Boolean isActive, Pageable pageable);
  
  boolean existsByUserIdAndContentId(String userId, Long contentId);
  
  long countByUserId(String userId);
}
