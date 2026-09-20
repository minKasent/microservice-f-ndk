package com.ndk.service.credit;

import com.ndk.service.credit.controller.CreditApiController;
import com.ndk.service.credit.dto.CreditBalanceDto;
import com.ndk.service.credit.service.CreditApiService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class BaseContractTest {

  @Mock
  private CreditApiService creditApiService;

  @InjectMocks
  private CreditApiController creditApiController;

  @BeforeEach
  public void setup() {
    RestAssuredMockMvc.standaloneSetup(creditApiController);

    Mockito.when(creditApiService.getBalance("user-123"))
        .thenReturn(CreditBalanceDto.builder()
            .userId("user-123")
            .balance(new BigDecimal("100.00"))
            .build());
  }
}
