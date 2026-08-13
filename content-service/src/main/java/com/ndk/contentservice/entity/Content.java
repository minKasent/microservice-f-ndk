package com.ndk.contentservice.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "content")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Content {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "creator_id", nullable = false)
  private Long creatorId; // User ID from identity-service

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private Category category;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ContentStatus status;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private ContentLevel level;

  @Column(precision = 10, scale = 2)
  private BigDecimal price; // Giá bán (credits)

  @Column(length = 255)
  private String thumbnail; // URL ảnh thumbnail

  @Column(name = "view_count")
  @Builder.Default
  private Long viewCount = 0L; // Tracking only, incremented when users view

  @Column(name = "purchase_count")
  @Builder.Default
  private Long purchaseCount = 0L; // Tracking only, incremented by purchase-service after successful purchase

  @Column(name = "published_at")
  private Instant publishedAt;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<ContentBlock> blocks = new ArrayList<>();
}
