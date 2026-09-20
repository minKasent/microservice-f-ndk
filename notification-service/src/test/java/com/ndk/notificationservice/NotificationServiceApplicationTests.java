package com.ndk.notificationservice;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Full context integration test requires running infrastructure (Config Server, Kafka, MongoDB)")
class NotificationServiceApplicationTests {

  @Test
  void contextLoads() {
  }

}
