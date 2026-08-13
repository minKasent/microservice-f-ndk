package com.ndk.rating.mapper;

import com.ndk.rating.dto.response.RatingDto;
import com.ndk.rating.entity.Rating;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    uses = {ReviewMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RatingMapper {
  RatingDto toDto(Rating rating);
}
