package com.ndk.common.api.exception.handler;

import com.ndk.common.api.exception.DevSharingException;
import com.ndk.common.api.exception.DevSharingExceptionInfo;
import com.ndk.common.api.response.ApiResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class DevSharingExceptionHandler {
  private final MessageSource messageSource;

  @ExceptionHandler(DevSharingException.class)
  public ResponseEntity<ApiResponse<?>> handleDevSharingException(
      DevSharingException ex) {
    DevSharingExceptionInfo exceptionInfo = ex.getExceptionInfo();

    String errorMessage = messageSource.getMessage(
        exceptionInfo.getErrorCode(),
        ex.getArgs(),
        exceptionInfo.getErrorMsg(),
        LocaleContextHolder.getLocale()
    );

    return ResponseEntity.status(exceptionInfo.getHttpStatus()).body(ApiResponse.error(
        errorMessage,
        exceptionInfo.getErrorCode()
    ));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<?>> handleValidationException(
      MethodArgumentNotValidException ex
  ) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach((error) -> {
      errors.put(error.getField(), error.getDefaultMessage());
    });

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(
        errors,
        null,
        "VALIDATION_ERROR"
    ));
  }
}
