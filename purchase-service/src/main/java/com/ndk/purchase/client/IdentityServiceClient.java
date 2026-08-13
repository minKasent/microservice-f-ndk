package com.ndk.purchase.client;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.purchase.config.FeignClientConfig;
import com.ndk.purchase.dto.feign.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "identity-service", 
    configuration = FeignClientConfig.class
)
public interface IdentityServiceClient {
  
  @GetMapping("/api/v1/users/{id}")
  ApiResponse<UserDto> getUserById(@PathVariable("id") String id);
}
