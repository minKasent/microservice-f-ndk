package com.ndk.contentservice.mapper;

import com.ndk.contentservice.dto.response.CategoryDto;
import com.ndk.contentservice.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CategoryMapper {
  CategoryDto toDto(Category category);
}
