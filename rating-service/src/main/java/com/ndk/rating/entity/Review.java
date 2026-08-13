package com.ndk.rating.entity;

import com.ndk.rating.enums.ReviewStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "rating_id", nullable = false)
  private Long ratingId;
  
  @Column(name = "content_id", nullable = false, length = 36)
  private String contentId;
  
  @Column(name = "user_id", nullable = false, length = 36)
  private String userId;
  
  @Column(length = 255)
  private String title;
  
  @Column(columnDefinition = "TEXT", nullable = false)
  private String content;
  
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private ReviewStatus status = ReviewStatus.ACTIVE;
  
  @Column(name = "is_verified_purchase")
  @Builder.Default
  private Boolean isVerifiedPurchase = false;
  
  @Column(name = "helpful_count")
  @Builder.Default
  private Integer helpfulCount = 0;
  
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
  
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
  
  @ManyToOne
  @JoinColumn(name = "rating_id", insertable = false, updatable = false)
  private Rating rating;
}
