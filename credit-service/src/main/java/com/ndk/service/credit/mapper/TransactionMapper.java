package com.ndk.service.credit.mapper;

import com.ndk.service.credit.dto.TransactionDto;
import com.ndk.service.credit.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface TransactionMapper {

  TransactionDto toDto(Transaction transaction);
}
