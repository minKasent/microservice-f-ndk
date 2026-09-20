package com.ndk.purchase;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Full context integration test requires running infrastructure (Config Server, MySQL, Kafka)")
class PurchaseServiceApplicationTests {

  @Test
  void contextLoads() {
  }

}
