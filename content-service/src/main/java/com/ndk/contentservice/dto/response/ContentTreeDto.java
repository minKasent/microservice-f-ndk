package com.ndk.contentservice.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentTreeDto {
  private Long contentId;
  private String title;
  private List<BlockDto> blocks; // Root blocks with nested children
}
