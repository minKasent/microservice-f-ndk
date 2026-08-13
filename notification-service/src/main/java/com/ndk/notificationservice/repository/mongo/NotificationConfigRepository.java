package com.ndk.notificationservice.repository.mongo;

import com.ndk.notificationservice.entity.mongo.NotificationConfig;
import com.ndk.notificationservice.enums.NotificationChannel;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationConfigRepository extends MongoRepository<NotificationConfig, String> {
  Optional<NotificationConfig> findByChannelAndIsActiveTrue(NotificationChannel channel);
}
