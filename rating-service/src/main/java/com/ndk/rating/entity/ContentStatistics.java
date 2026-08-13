package com.ndk.rating.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "content_statistics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentStatistics {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "content_id", nullable = false, unique = true, length = 36)
  private String contentId;
  
  @Column(name = "total_ratings")
  @Builder.Default
  private Integer totalRatings = 0;
  
  @Column(name = "average_rating", precision = 3, scale = 2)
  @Builder.Default
  private BigDecimal averageRating = BigDecimal.ZERO;
  
  @Column(name = "rating_1_count")
  @Builder.Default
  private Integer rating1Count = 0;
  
  @Column(name = "rating_2_count")
  @Builder.Default
  private Integer rating2Count = 0;
  
  @Column(name = "rating_3_count")
  @Builder.Default
  private Integer rating3Count = 0;
  
  @Column(name = "rating_4_count")
  @Builder.Default
  private Integer rating4Count = 0;
  
  @Column(name = "rating_5_count")
  @Builder.Default
  private Integer rating5Count = 0;
  
  @Column(name = "total_reviews")
  @Builder.Default
  private Integer totalReviews = 0;
  
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
