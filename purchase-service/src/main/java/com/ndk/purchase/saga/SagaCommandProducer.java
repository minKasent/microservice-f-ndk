package com.ndk.purchase.saga;

import com.ndk.common.saga.message.SagaCommand;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaCommandProducer {

  private final KafkaTemplate<String, SagaCommand> sagaCommandKafkaTemplate;
  private static final int KAFKA_SEND_TIMEOUT_SECONDS = 10;

  @Value("${purchase.saga.credit-command-topic:purchase.credit-command}")
  private String creditCommandTopic;

  @Value("${purchase.saga.content-command-topic:purchase.content-command}")
  private String contentCommandTopic;

  public void sendCreditCommand(SagaCommand command) {
    sendCommand(command, creditCommandTopic);
  }

  public void sendContentCommand(SagaCommand command) {
    sendCommand(command, contentCommandTopic);
  }

  private void sendCommand(SagaCommand command, String topic) {
    try {
      sagaCommandKafkaTemplate.send(topic, command.getSagaId(), command)
          .get(KAFKA_SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    } catch (Exception e) {
      log.error("Send command queue failed, command: {}", command, e);
    }
  }
}
