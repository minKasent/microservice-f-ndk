package com.ndk.notificationservice.entity.mongo;

import com.ndk.notificationservice.enums.NotificationChannel;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notification_template")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplate {
  @Id
  private String id;

  @Indexed(unique = true)
  private String templateId;

  private String name;
  private NotificationChannel channel;
  private String subject;
  private String content;
  private List<String> variables;

  @Builder.Default
  private Boolean isActive = true;

  private Date createdAt;
  private Date updatedAt;
}
