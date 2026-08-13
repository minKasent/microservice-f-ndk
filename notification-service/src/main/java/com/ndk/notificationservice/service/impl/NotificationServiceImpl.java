package com.ndk.notificationservice.service.impl;

import com.ndk.common.api.logging.LogExecutionTime;

import com.ndk.common.api.exception.DevSharingException;
import com.ndk.notificationservice.dto.request.SendBulkNotificationRequest;
import com.ndk.notificationservice.dto.request.SendNotificationRequest;
import com.ndk.notificationservice.dto.response.NotificationDto;
import com.ndk.notificationservice.entity.mongo.NotificationHistory;
import com.ndk.notificationservice.enums.NotificationStatus;
import com.ndk.notificationservice.exception.ExceptionEnum;
import com.ndk.notificationservice.kafka.message.NotificationMessage;
import com.ndk.notificationservice.kafka.producer.NotificationProducer;
import com.ndk.notificationservice.mapper.NotificationMapper;
import com.ndk.notificationservice.repository.mongo.NotificationHistoryRepository;
import com.ndk.notificationservice.service.NotificationDispatcher;
import com.ndk.notificationservice.service.NotificationService;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@LogExecutionTime
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

  private final NotificationProducer producer;
  private final NotificationHistoryRepository historyRepository;
  private final NotificationMapper mapper;
  private final NotificationDispatcher dispatcher;

  @Override
  public NotificationDto send(SendNotificationRequest request) {
    String notificationId = UUID.randomUUID().toString();

    NotificationHistory history = NotificationHistory.builder()
        .notificationId(notificationId)
        .channel(request.getChannel())
        .recipient(request.getRecipient())
        .subject(request.getSubject())
        .content(request.getContent())
        .templateId(request.getTemplateId())
        .templateData(request.getTemplateData())
        .status(NotificationStatus.PENDING)
        .metadata(request.getMetadata())
        .retryCount(0)
        .maxRetries(3)
        .createdAt(new Date())
        .updatedAt(new Date())
        .build();

    history = historyRepository.save(history);

    NotificationMessage message = NotificationMessage.builder()
        .notificationId(notificationId)
        .channel(request.getChannel())
        .recipient(request.getRecipient())
        .subject(request.getSubject())
        .content(request.getContent())
        .templateId(request.getTemplateId())
        .templateData(request.getTemplateData())
        .metadata(request.getMetadata())
        .priority(request.getPriority())
        .build();

    producer.send(message)
        .exceptionally(ex -> {
          log.error("Failed to send to Kafka: {}", notificationId, ex);
          throw new DevSharingException(ExceptionEnum.KAFKA_SEND_FAILED,
              new Object[]{notificationId});
        });

    return mapper.toDto(history);
  }

  @Override
  public void sendBulk(SendBulkNotificationRequest request) {
    if (request.getRecipients().size() > 10000) {
      throw new DevSharingException(ExceptionEnum.BULK_SIZE_EXCEEDED,
          new Object[]{"10000", String.valueOf(request.getRecipients().size())});
    }

    request.getRecipients().parallelStream()
        .forEach(recipient -> {
          SendNotificationRequest singleRequest = SendNotificationRequest.builder()
              .channel(request.getChannel())
              .recipient(recipient)
              .subject(request.getSubject())
              .content(request.getContent())
              .templateId(request.getTemplateId())
              .templateData(request.getTemplateData())
              .build();

          send(singleRequest);
        });
  }

  @Override
  public Page<NotificationDto> getHistory(String recipient, Pageable pageable) {
    return historyRepository.findByRecipient(recipient, pageable)
        .map(mapper::toDto);
  }

  @Override
//  @Async
  public void processRetry(List<NotificationHistory> notificationHistories) {
    try {
      log.info("Processing {} retryable notifications", notificationHistories.size());
      notificationHistories.forEach(history -> {
        if (history.getRetryCount() >= history.getMaxRetries()) {
          history.setStatus(NotificationStatus.DEAD_LETTER);
          historyRepository.save(history);
          log.warn("Notification moved to dead letter: {}", history.getNotificationId());
          return;
        }

        history.setRetryCount(history.getRetryCount() + 1);
        history.setStatus(NotificationStatus.PENDING);
        historyRepository.save(history);

        NotificationMessage message = NotificationMessage.builder()
            .notificationId(history.getNotificationId())
            .channel(history.getChannel())
            .recipient(history.getRecipient())
            .subject(history.getSubject())
            .content(history.getContent())
            .templateId(history.getTemplateId())
            .templateData(history.getTemplateData())
            .metadata(history.getMetadata())
            .build();

        dispatcher.dispatch(message);
      });

    }catch (Exception e) {
      log.error("Error processing retry with message: [{}]", e.getMessage(),e);
    }

  }
}
