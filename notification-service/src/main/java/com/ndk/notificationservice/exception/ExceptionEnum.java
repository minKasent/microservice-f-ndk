package com.ndk.notificationservice.exception;

import com.ndk.common.api.exception.DevSharingExceptionInfo;
import org.springframework.http.HttpStatus;

public enum ExceptionEnum implements DevSharingExceptionInfo {
  NOTIFICATION_NOT_FOUND("NOTIFICATION_001", "error.notification.notFound", HttpStatus.NOT_FOUND),
  INVALID_RECIPIENT("NOTIFICATION_002", "error.notification.invalidRecipient", HttpStatus.BAD_REQUEST),
  INVALID_CHANNEL("NOTIFICATION_003", "error.notification.invalidChannel", HttpStatus.BAD_REQUEST),
  SEND_FAILED("NOTIFICATION_004", "error.notification.sendFailed", HttpStatus.INTERNAL_SERVER_ERROR),

  TEMPLATE_NOT_FOUND("TEMPLATE_001", "error.template.notFound", HttpStatus.NOT_FOUND),
  TEMPLATE_INVALID("TEMPLATE_002", "error.template.invalid", HttpStatus.BAD_REQUEST),
  TEMPLATE_RENDERING_FAILED("TEMPLATE_003", "error.template.renderingFailed",
      HttpStatus.INTERNAL_SERVER_ERROR),

  PROVIDER_NOT_CONFIGURED("PROVIDER_001", "error.provider.notConfigured",
      HttpStatus.SERVICE_UNAVAILABLE),
  PROVIDER_RATE_LIMIT_EXCEEDED("PROVIDER_002", "error.provider.rateLimitExceeded",
      HttpStatus.TOO_MANY_REQUESTS),
  PROVIDER_AUTHENTICATION_FAILED("PROVIDER_003", "error.provider.authenticationFailed",
      HttpStatus.UNAUTHORIZED),

  KAFKA_SEND_FAILED("KAFKA_001", "error.kafka.sendFailed", HttpStatus.INTERNAL_SERVER_ERROR),

  BULK_SIZE_EXCEEDED("BULK_001", "error.bulk.sizeExceeded", HttpStatus.BAD_REQUEST);

  private final String errorCode;
  private final String errorMsg;
  private final HttpStatus httpStatus;

  ExceptionEnum(String errorCode, String errorMsg, HttpStatus httpStatus) {
    this.errorCode = errorCode;
    this.errorMsg = errorMsg;
    this.httpStatus = httpStatus;
  }

  @Override
  public String getErrorCode() {
    return errorCode;
  }

  @Override
  public String getErrorMsg() {
    return errorMsg;
  }

  @Override
  public HttpStatus getHttpStatus() {
    return httpStatus;
  }
}
