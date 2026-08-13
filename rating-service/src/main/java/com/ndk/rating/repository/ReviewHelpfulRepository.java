package com.ndk.rating.repository;

import com.ndk.rating.entity.ReviewHelpful;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewHelpfulRepository extends JpaRepository<ReviewHelpful, Long> {
  boolean existsByReviewIdAndUserId(Long reviewId, String userId);
}
