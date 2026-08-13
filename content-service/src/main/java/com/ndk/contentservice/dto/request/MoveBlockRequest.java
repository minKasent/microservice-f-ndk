package com.ndk.contentservice.dto.request;

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
public class MoveBlockRequest {
  private Long newParentBlockId; // null = move to root

  @NotNull(message = "{block.position.notNull}")
  @Min(value = 1, message = "{block.position.min}")
  private Integer newPosition;
}
