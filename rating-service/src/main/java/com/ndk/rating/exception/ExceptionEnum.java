package com.ndk.rating.exception;

import com.ndk.common.api.exception.DevSharingExceptionInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExceptionEnum implements DevSharingExceptionInfo {
  RATING_NOT_FOUND("RATING_001", "error.rating.notFound", HttpStatus.NOT_FOUND),
  INVALID_RATING_VALUE("RATING_002", "error.rating.invalidValue", HttpStatus.BAD_REQUEST),
  DUPLICATE_RATING("RATING_003", "error.rating.duplicate", HttpStatus.CONFLICT),
  
  REVIEW_NOT_FOUND("REVIEW_001", "error.review.notFound", HttpStatus.NOT_FOUND),
  REVIEW_ALREADY_EXISTS("REVIEW_002", "error.review.alreadyExists", HttpStatus.CONFLICT),
  REVIEW_ALREADY_MARKED_HELPFUL("REVIEW_003", "error.review.alreadyMarkedHelpful", HttpStatus.CONFLICT),
  
  CONTENT_NOT_FOUND("CONTENT_001", "error.content.notFound", HttpStatus.NOT_FOUND),
  USER_NOT_FOUND("USER_001", "error.user.notFound", HttpStatus.NOT_FOUND),
  UNAUTHORIZED_ACTION("AUTH_001", "error.auth.unauthorized", HttpStatus.FORBIDDEN);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;

  @Override
  public String getErrorCode() {
    return code;
  }

  @Override
  public String getErrorMsg() {
    return message;
  }
}
