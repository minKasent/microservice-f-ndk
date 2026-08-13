package com.ndk.rating.mapper;

import com.ndk.rating.dto.response.ContentStatisticsDto;
import com.ndk.rating.entity.ContentStatistics;
import java.util.HashMap;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ContentStatisticsMapper {
  @Mapping(target = "ratingDistribution", expression = "java(buildRatingDistribution(stats))")
  ContentStatisticsDto toDto(ContentStatistics stats);
  
  default Map<Integer, Integer> buildRatingDistribution(ContentStatistics stats) {
    Map<Integer, Integer> distribution = new HashMap<>();
    distribution.put(1, stats.getRating1Count());
    distribution.put(2, stats.getRating2Count());
    distribution.put(3, stats.getRating3Count());
    distribution.put(4, stats.getRating4Count());
    distribution.put(5, stats.getRating5Count());
    return distribution;
  }
}
