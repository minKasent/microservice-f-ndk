package com.ndk.purchase.mapper;

import com.ndk.purchase.dto.response.PurchaseTransactionDto;
import com.ndk.purchase.entity.PurchaseTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PurchaseTransactionMapper {
  PurchaseTransactionDto toDto(PurchaseTransaction transaction);
}
