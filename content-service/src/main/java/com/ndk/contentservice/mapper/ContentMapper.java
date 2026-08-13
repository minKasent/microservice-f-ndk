package com.ndk.contentservice.mapper;

import com.ndk.contentservice.dto.response.ContentDto;
import com.ndk.contentservice.dto.response.ContentSummaryDto;
import com.ndk.contentservice.entity.Content;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    uses = {CategoryMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ContentMapper {
  
  // Don't map blocks automatically - let service layer handle it
  @Mapping(target = "blocks", ignore = true)
  ContentDto toDto(Content content);
  
  ContentSummaryDto toSummaryDto(Content content);
}
