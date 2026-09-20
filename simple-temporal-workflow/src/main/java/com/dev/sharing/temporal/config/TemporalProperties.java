package com.dev.sharing.temporal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "temporal")
public class TemporalProperties {
  private String target;
  private String namespace;
  private String taskQueue;
}
