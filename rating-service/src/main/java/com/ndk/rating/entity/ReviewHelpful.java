package com.ndk.rating.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_helpful")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewHelpful {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "review_id", nullable = false)
  private Long reviewId;
  
  @Column(name = "user_id", nullable = false, length = 36)
  private String userId;
  
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}
