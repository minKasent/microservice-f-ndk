package com.ndk.rating.repository;

import com.ndk.rating.entity.Review;
import com.ndk.rating.enums.ReviewStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
  Optional<Review> findByRatingId(Long ratingId);
  
  Page<Review> findByContentIdAndStatus(String contentId, ReviewStatus status, Pageable pageable);
  
  Page<Review> findByUserId(String userId, Pageable pageable);
  
  long countByContentIdAndStatus(String contentId, ReviewStatus status);
}
