package com.ndk.service.credit.mapper;

import com.ndk.service.credit.dto.WithdrawalRequestDto;
import com.ndk.service.credit.entity.WithdrawalRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface WithdrawalRequestMapper {

  WithdrawalRequestDto toDto(WithdrawalRequest withdrawalRequest);
}
