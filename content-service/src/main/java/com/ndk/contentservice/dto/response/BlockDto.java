package com.ndk.contentservice.dto.response;

import com.ndk.contentservice.entity.BlockType;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockDto {
  private Long id;
  private Long contentId;
  private Long parentBlockId;
  private BlockType type;
  private String textContent;
  private String properties; // JSON string
  private Integer position;
  private Boolean isFree;
  private Date createdAt;
  private Date updatedAt;
  private List<BlockDto> children; // Nested children for tree structure
}
