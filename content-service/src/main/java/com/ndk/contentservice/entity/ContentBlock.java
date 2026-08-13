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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "content_block")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentBlock {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "content_id", nullable = false)
  private Content content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_block_id")
  private ContentBlock parentBlock; // Self-referencing for tree structure

  @OneToMany(mappedBy = "parentBlock", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("position ASC")
  @Builder.Default
  private List<ContentBlock> children = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private BlockType type;

  @Column(name = "text_content", columnDefinition = "LONGTEXT")
  private String textContent; // Block content (text, code, etc.)

  @Column(columnDefinition = "JSON")
  private String properties; // JSON metadata: {language: "java", url: "...", color: "blue"}

  @Column(nullable = false)
  private Integer position; // Order within same parent

  @Column(name = "is_free")
  @Builder.Default
  private Boolean isFree = false; // Free preview block

  @Column(name = "created_at", nullable = false)
  private Date createdAt;

  @Column(name = "updated_at", nullable = false)
  private Date updatedAt;
}
