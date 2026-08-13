package com.ndk.identityservice.mapper;

import com.ndk.identityservice.dto.response.CreatorApplicationResponseDto;
import com.ndk.identityservice.entity.CreatorApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface CreatorApplicationMapper {

  @Mapping(source = "user.id", target = "userId")
  @Mapping(source = "user.username", target = "username")
  @Mapping(source = "user.email", target = "email")
  CreatorApplicationResponseDto toResponseDto(CreatorApplication application);
}
