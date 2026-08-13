package com.ndk.rating.repository;

import com.ndk.rating.entity.Rating;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<Rating, Long> {
  Optional<Rating> findByContentIdAndUserId(String contentId, String userId);
  
  Page<Rating> findByUserId(String userId, Pageable pageable);
  
  Page<Rating> findByContentId(String contentId, Pageable pageable);
  
  boolean existsByContentIdAndUserId(String contentId, String userId);
  
  void deleteByContentIdAndUserId(String contentId, String userId);
}
