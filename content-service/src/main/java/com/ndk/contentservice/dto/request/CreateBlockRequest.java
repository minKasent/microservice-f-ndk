package com.ndk.contentservice.dto.request;

import com.ndk.contentservice.entity.BlockType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBlockRequest {
  private Long parentBlockId; // null = root block

  @NotNull(message = "{block.type.notNull}")
  private BlockType type;

  private String textContent; // Block content (text, code, etc.)

  private String properties; // JSON string: {language: "java", url: "...", color: "blue"}

  @NotNull(message = "{block.position.notNull}")
  @Min(value = 1, message = "{block.position.min}")
  private Integer position;

  @Builder.Default
  private Boolean isFree = false;
}
