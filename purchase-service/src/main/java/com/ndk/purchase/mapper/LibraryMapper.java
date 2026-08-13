package com.ndk.purchase.mapper;

import com.ndk.purchase.dto.response.LibraryItemDto;
import com.ndk.purchase.entity.Library;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LibraryMapper {
  @Mapping(target = "contentTitle", ignore = true)
  @Mapping(target = "contentThumbnail", ignore = true)
  @Mapping(target = "creatorName", ignore = true)
  @Mapping(target = "purchasePrice", ignore = true)
  @Mapping(target = "purchasedAt", source = "accessGrantedAt")
  LibraryItemDto toDto(Library library);
}
