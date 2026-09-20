package com.ndk.purchase.config;

import com.ndk.common.saga.message.SagaCommand;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class SagaKafkaConfig {

  @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
  private String bootstrapServers;

  @Value("${purchase.saga.credit-command-topic:purchase.credit-command}")
  private String creditCommandTopic;

  @Value("${purchase.saga.content-command-topic:purchase.content-command}")
  private String contentCommandTopic;

  @Value("${purchase.saga.reply-topic:purchase.saga-replies}")
  private String replyTopic;

  @Bean
  public NewTopic creditCommandTopic() {
    return TopicBuilder.name(creditCommandTopic)
        .partitions(3)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic contentCommandTopic() {
    return TopicBuilder.name(contentCommandTopic)
        .partitions(3)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic sagaReplyTopic() {
    return TopicBuilder.name(replyTopic)
        .partitions(3)
        .replicas(1)
        .build();
  }

  @Bean
  public ProducerFactory<String, SagaCommand> sagaCommandProducerFactory() {
    Map<String, Object> config = new HashMap<>();
    config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    config.put(ProducerConfig.ACKS_CONFIG, "all");
    config.put(ProducerConfig.RETRIES_CONFIG, 3);
    config.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
    config.put(ProducerConfig.LINGER_MS_CONFIG, 10);
    return new DefaultKafkaProducerFactory<>(config);
  }

  @Bean
  public KafkaTemplate<String, SagaCommand> sagaCommandKafkaTemplate() {
    return new KafkaTemplate<>(sagaCommandProducerFactory());
  }
}
