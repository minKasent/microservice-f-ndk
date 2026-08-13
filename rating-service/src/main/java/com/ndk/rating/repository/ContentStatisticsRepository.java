package com.ndk.rating.repository;

import com.ndk.rating.entity.ContentStatistics;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentStatisticsRepository extends JpaRepository<ContentStatistics, Long> {
  Optional<ContentStatistics> findByContentId(String contentId);
}
