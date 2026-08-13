package com.ndk.notificationservice.repository.mongo;

import com.ndk.notificationservice.entity.mongo.NotificationHistory;
import com.ndk.notificationservice.enums.NotificationStatus;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface NotificationHistoryRepository extends MongoRepository<NotificationHistory, String> {
  Optional<NotificationHistory> findByNotificationId(String notificationId);

  Page<NotificationHistory> findByRecipient(String recipient, Pageable pageable);

  @Query("{ 'status': ?0, 'nextRetryAt': { $lte: ?1 } }")
  List<NotificationHistory> findRetryable(NotificationStatus status, Date now);


  @Query("{ 'status': ?0 }")
  List<NotificationHistory> findRetryable(NotificationStatus status);
  long countByStatus(NotificationStatus status);
}
