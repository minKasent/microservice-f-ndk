package com.ndk.contentservice.mapper;

import com.ndk.contentservice.dto.response.ContentReviewDto;
import com.ndk.contentservice.entity.ContentReview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ContentReviewMapper {
  @Mapping(source = "content.id", target = "contentId")
  ContentReviewDto toDto(ContentReview contentReview);
}
