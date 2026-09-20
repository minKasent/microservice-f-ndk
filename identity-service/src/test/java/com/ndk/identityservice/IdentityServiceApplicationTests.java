package com.ndk.identityservice;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Full context integration test requires running infrastructure (Config Server, MySQL)")
class IdentityServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
