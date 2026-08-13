package com.ndk.identityservice.mapper;

import com.ndk.identityservice.dto.response.UserRegistrationResponseDto;
import com.ndk.identityservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface UserRegistrationMapper {
 UserRegistrationResponseDto toResponseDto(User user);  // map từ entity sang dto

}
