package com.ndk.purchase.mapper;

import com.ndk.purchase.dto.response.PurchaseDto;
import com.ndk.purchase.entity.Purchase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PurchaseMapper {
  @Mapping(target = "contentTitle", ignore = true)
  @Mapping(target = "creatorName", ignore = true)
  PurchaseDto toDto(Purchase purchase);
}
