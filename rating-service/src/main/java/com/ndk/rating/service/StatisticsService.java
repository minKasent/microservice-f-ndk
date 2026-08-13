package com.ndk.rating.service;

import com.ndk.rating.dto.response.ContentStatisticsDto;

public interface StatisticsService {
  ContentStatisticsDto getContentStatistics(String contentId);
  
  void updateStatistics(String contentId);
}
