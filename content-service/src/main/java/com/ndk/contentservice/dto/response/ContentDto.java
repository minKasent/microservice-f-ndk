package com.ndk.contentservice.dto.response;

import com.ndk.contentservice.entity.ContentLevel;
import com.ndk.contentservice.entity.ContentStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentDto {
  private Long id;
  private String title;
  private String description;
  private Long creatorId;
  private CategoryDto category;
  private ContentStatus status;
  private ContentLevel level;
  private BigDecimal price;
  private String thumbnail;
  private Long viewCount;
  private Long purchaseCount;
  private Instant publishedAt;
  private Instant createdAt;
  private Instant updatedAt;
  private List<BlockDto> blocks; // Root blocks with nested children
}
