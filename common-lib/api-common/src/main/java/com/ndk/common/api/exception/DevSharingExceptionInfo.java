package com.ndk.common.api.exception;

import org.springframework.http.HttpStatus;

public interface DevSharingExceptionInfo {
  String getErrorCode();
  String getErrorMsg();
  HttpStatus getHttpStatus();
}
