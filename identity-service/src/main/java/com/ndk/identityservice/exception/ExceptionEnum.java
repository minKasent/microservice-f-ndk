package com.ndk.identityservice.exception;

import com.ndk.common.api.exception.DevSharingExceptionInfo;
import org.springframework.http.HttpStatus;

public enum ExceptionEnum implements DevSharingExceptionInfo {
  USERNAME_EXISTED_ERROR(
      "USERNAME_EXISTED_ERROR",
      "Username already existed in the system",
      HttpStatus.BAD_REQUEST
  ),
  ROLE_NOT_FOUND_ERROR(
      "ROLE_NOT_FOUND_ERROR",
      "Role not found in the system",
      HttpStatus.INTERNAL_SERVER_ERROR
  ),
  USER_NOT_FOUND_ERROR(
      "USER_NOT_FOUND_ERROR",
      "User not found in the system",
      HttpStatus.NOT_FOUND
  ),
  APPLICATION_NOT_FOUND_ERROR(
      "APPLICATION_NOT_FOUND_ERROR",
      "Creator application not found",
      HttpStatus.NOT_FOUND
  ),
  PENDING_APPLICATION_EXISTS_ERROR(
      "PENDING_APPLICATION_EXISTS_ERROR",
      "User already has a pending application",
      HttpStatus.BAD_REQUEST
  ),
  USER_ALREADY_CREATOR_ERROR(
      "USER_ALREADY_CREATOR_ERROR",
      "User is already a content creator",
      HttpStatus.BAD_REQUEST
  ),
  INVALID_APPLICATION_STATUS_ERROR(
      "INVALID_APPLICATION_STATUS_ERROR",
      "Invalid application status for this operation",
      HttpStatus.BAD_REQUEST
  ),
  USER_VERIFICATION_CODE_NOT_FOUND_ERROR(
      "USER_VERIFICATION_CODE_NOT_FOUND_ERROR",
      "Verification code not found",
      HttpStatus.NOT_FOUND
  ),
  USER_VERIFICATION_CODE_EXPIRED_ERROR(
      "USER_VERIFICATION_CODE_EXPIRED_ERROR",
      "Verification code has expired",
      HttpStatus.BAD_REQUEST
  ),
  UNAUTHORIZED_REVIEW_ERROR(
      "UNAUTHORIZED_REVIEW_ERROR",
      "Only moderators can review applications",
      HttpStatus.FORBIDDEN
  );

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
    return this.errorCode;
  }

  @Override
  public String getErrorMsg() {
    return this.errorMsg;
  }

  @Override
  public HttpStatus getHttpStatus() {
    return this.httpStatus;
  }
}
