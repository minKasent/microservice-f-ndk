package com.ndk.contentservice.dto.request;

import com.ndk.contentservice.entity.BlockType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBlockRequest {
  private BlockType type;
  private String textContent;
  private String properties;
  private Boolean isFree;
}
