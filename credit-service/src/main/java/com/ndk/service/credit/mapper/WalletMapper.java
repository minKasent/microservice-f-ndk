package com.ndk.service.credit.mapper;

import com.ndk.service.credit.dto.WalletDto;
import com.ndk.service.credit.entity.Wallet;
import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE
)
public interface WalletMapper {

  @Mapping(source = "balance", target = "balanceInVnd", qualifiedByName = "creditToVnd")
  WalletDto toDto(Wallet wallet);

  @Named("creditToVnd")
  default BigDecimal creditToVnd(BigDecimal credit) {
    if (credit == null) {
      return BigDecimal.ZERO;
    }
    return credit.multiply(new BigDecimal("1000"));
  }
}
