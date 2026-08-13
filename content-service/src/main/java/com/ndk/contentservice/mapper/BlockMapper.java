package com.ndk.contentservice.mapper;

import com.ndk.contentservice.dto.response.BlockDto;
import com.ndk.contentservice.entity.ContentBlock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BlockMapper {
  
  // Default mapping with children (for tree structure)
  @Mapping(source = "content.id", target = "contentId")
  @Mapping(source = "parentBlock.id", target = "parentBlockId")
  @Mapping(source = "children", target = "children")
  BlockDto toDto(ContentBlock block);
}
