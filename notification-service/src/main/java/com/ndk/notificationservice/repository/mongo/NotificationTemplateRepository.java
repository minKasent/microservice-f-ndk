package com.ndk.notificationservice.repository.mongo;

import com.ndk.notificationservice.entity.mongo.NotificationTemplate;
import com.ndk.notificationservice.enums.NotificationChannel;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationTemplateRepository extends MongoRepository<NotificationTemplate, String> {
  Optional<NotificationTemplate> findByTemplateIdAndIsActiveTrue(String templateId);

  List<NotificationTemplate> findByChannelAndIsActiveTrue(NotificationChannel channel);
}
