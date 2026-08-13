package com.ndk.purchase.client;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.purchase.config.FeignClientConfig;
import com.ndk.purchase.dto.feign.AddCreditRequest;
import com.ndk.purchase.dto.feign.CreditBalanceDto;
import com.ndk.purchase.dto.feign.CreditTransactionDto;
import com.ndk.purchase.dto.feign.DeductCreditRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "credit-service", 
    configuration = FeignClientConfig.class
)
public interface CreditServiceClient {
  
  @GetMapping("/api/v1/wallets/balance/{userId}")
  ApiResponse<CreditBalanceDto> getBalance(@PathVariable("userId") String userId);
  
  @PostMapping("/api/v1/transactions/deduct")
  ApiResponse<CreditTransactionDto> deduct(@RequestBody DeductCreditRequest request);
  
  @PostMapping("/api/v1/transactions/add")
  ApiResponse<CreditTransactionDto> add(@RequestBody AddCreditRequest request);
}
