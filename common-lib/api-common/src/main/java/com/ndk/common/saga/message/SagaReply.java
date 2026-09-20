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
public class SagaReply {
  private String sagaId;
  private String commandType;
  private boolean success;
  private String errorCode;
  private String errorMessage;
  private Instant timestamp;
  private Map<String, Object> payload;
}
