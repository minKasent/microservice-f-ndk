package com.ndk.purchase.client.adapter;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.purchase.client.CreditServiceClient;
import com.ndk.purchase.dto.feign.AddCreditRequest;
import com.ndk.purchase.dto.feign.CreditBalanceDto;
import com.ndk.purchase.dto.feign.CreditTransactionDto;
import com.ndk.purchase.dto.feign.DeductCreditRequest;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Theo docs Resilience4j (default aspect order):
 * Retry ( CircuitBreaker ( RateLimiter ( Bulkhead ( Function ) ) ) )
 *
 * @see <a href="https://resilience4j.readme.io/docs/getting-started-3">Resilience4j Spring Boot</a>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CreditServiceClientAdapter {

  private final CreditServiceClient creditServiceClient;

  private static final String CREDIT_SERVICE = "credit-service";

  @Retry(name = CREDIT_SERVICE)
  @CircuitBreaker(name = CREDIT_SERVICE, fallbackMethod = "getBalanceCallback")
  @RateLimiter(name = CREDIT_SERVICE)
  @Bulkhead(name = CREDIT_SERVICE)
  public ApiResponse<CreditBalanceDto> getBalance(String userId) {
    log.info("(getBalance)Get credit balance by userId: [{}]", userId);
    return creditServiceClient.getBalance(userId);
  }

  private ApiResponse<CreditBalanceDto> getBalanceCallback(String userId, Throwable ex) {
    log.error("(getBalanceCallback)Circuit breaker fallback for userId: [{}], error: {}",
        userId, ex.getMessage());
    CreditBalanceDto fallbackBalance = CreditBalanceDto.builder()
        .userId(userId)
        .balance(BigDecimal.ZERO)
        .build();
    return ApiResponse.success(fallbackBalance);
  }

  @Retry(name = CREDIT_SERVICE)
  @CircuitBreaker(name = CREDIT_SERVICE)
  @RateLimiter(name = CREDIT_SERVICE)
  @Bulkhead(name = CREDIT_SERVICE)
  public ApiResponse<CreditTransactionDto> deduct(DeductCreditRequest request) {
    log.info("(deduct)Deduct credit for userId: [{}], amount: [{}]", request.getUserId(),
        request.getAmount());
    return creditServiceClient.deduct(request);
  }

  @Retry(name = CREDIT_SERVICE)
  @CircuitBreaker(name = CREDIT_SERVICE)
  @RateLimiter(name = CREDIT_SERVICE)
  @Bulkhead(name = CREDIT_SERVICE)
  public ApiResponse<CreditTransactionDto> add(AddCreditRequest request) {
    log.info("(add)Add credit for userId: [{}], amount: [{}]", request.getUserId(),
        request.getAmount());
    return creditServiceClient.add(request);
  }
}
