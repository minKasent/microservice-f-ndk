package com.ndk.common.saga.message;

import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaCommand {
  private String sagaId;
  private String commandType;
  private String idempotencyKey;
  private String replyTopic;
  private Instant timestamp;
  private Map<String, Object> payload;
}
