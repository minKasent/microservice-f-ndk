package com.ndk.rating.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rating")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rating {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "content_id", nullable = false, length = 36)
  private String contentId;
  
  @Column(name = "user_id", nullable = false, length = 36)
  private String userId;
  
  @Column(name = "rating_value", nullable = false)
  private Integer ratingValue;
  
  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
  
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
  
  @OneToOne(mappedBy = "rating")
  private Review review;
}
