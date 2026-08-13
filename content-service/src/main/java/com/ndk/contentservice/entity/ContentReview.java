package com.ndk.contentservice.entity;

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
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ContentReview - Moderator review for content approval workflow
 * 
 * ⚠️ IMPORTANT: This is NOT user review/rating!
 * - This is for MODERATOR to approve/reject content before publishing
 * - User review/rating is handled by rating-service
 * 
 * Workflow: Creator submits content → Moderator reviews → Approve/Reject/Request Changes
 */
@Entity
@Table(name = "content_review")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentReview {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "content_id", nullable = false)
  private Content content;

  @Column(name = "moderator_id", nullable = false)
  private Long moderatorId; // User ID of the moderator who reviewed

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ReviewAction action; // APPROVE, REJECT, REQUEST_CHANGES

  @Column(columnDefinition = "TEXT")
  private String feedback; // Moderator's feedback/reason

  @Column(name = "reviewed_at", nullable = false)
  private Instant reviewedAt;
}
