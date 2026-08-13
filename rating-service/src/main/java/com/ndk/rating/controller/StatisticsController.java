package com.ndk.rating.controller;

import com.ndk.common.api.response.ApiResponse;
import com.ndk.rating.dto.response.ContentStatisticsDto;
import com.ndk.rating.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {
  private final StatisticsService statisticsService;
  
  @GetMapping("/content/{contentId}")
  public ResponseEntity<ApiResponse<ContentStatisticsDto>> getContentStatistics(
      @PathVariable String contentId
  ) {
    ContentStatisticsDto statistics = statisticsService.getContentStatistics(contentId);
    return ResponseEntity.ok(ApiResponse.success(statistics));
  }
}
