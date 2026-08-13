package com.ndk.rating.mapper;

import com.ndk.rating.dto.response.ReviewDto;
import com.ndk.rating.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ReviewMapper {
  @Mapping(target = "ratingValue", source = "rating.ratingValue")
  @Mapping(target = "userDisplayName", ignore = true)
  ReviewDto toDto(Review review);
}
