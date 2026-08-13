package com.ndk.contentservice.repository;

import com.ndk.contentservice.entity.Content;
import com.ndk.contentservice.entity.ContentReview;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentReviewRepository extends JpaRepository<ContentReview, Long> {
  List<ContentReview> findByContentIdOrderByReviewedAtDesc(Long contentId);
  
  List<ContentReview> findByContentOrderByReviewedAtDesc(Content content);
}
