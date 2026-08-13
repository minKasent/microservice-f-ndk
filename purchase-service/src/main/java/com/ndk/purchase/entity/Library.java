package com.ndk.purchase.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "user_library")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Library {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(name = "user_id", nullable = false, length = 36)
  private String userId;
  
  @Column(name = "content_id", nullable = false)
  private Long contentId;  // Changed from String to Long to match content-service
  
  @Column(name = "purchase_id", nullable = false)
  private Long purchaseId;
  
  @Column(name = "access_granted_at", nullable = false)
  private Instant accessGrantedAt;
  
  @Column(name = "last_accessed_at")
  private Instant lastAccessedAt;
  
  @Column(name = "access_count")
  @Builder.Default
  private Integer accessCount = 0;
  
  @Column(name = "is_active")
  @Builder.Default
  private Boolean isActive = true;
  
  @ManyToOne
  @JoinColumn(name = "purchase_id", insertable = false, updatable = false)
  private Purchase purchase;
}
