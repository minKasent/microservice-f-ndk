package com.ndk.service.credit.exception;

import com.ndk.common.api.exception.DevSharingExceptionInfo;
import org.springframework.http.HttpStatus;

public enum ExceptionEnum implements DevSharingExceptionInfo {
  WALLET_NOT_FOUND(
      "WALLET_NOT_FOUND",
      "Wallet not found for user",
      HttpStatus.NOT_FOUND
  ),
  INSUFFICIENT_BALANCE(
      "INSUFFICIENT_BALANCE",
      "Insufficient balance in wallet",
      HttpStatus.BAD_REQUEST
  ),
  INVALID_AMOUNT(
      "INVALID_AMOUNT",
      "Invalid amount, must be positive",
      HttpStatus.BAD_REQUEST
  ),
  WITHDRAWAL_REQUEST_NOT_FOUND(
      "WITHDRAWAL_REQUEST_NOT_FOUND",
      "Withdrawal request not found",
      HttpStatus.NOT_FOUND
  ),
  WITHDRAWAL_ALREADY_PROCESSED(
      "WITHDRAWAL_ALREADY_PROCESSED",
      "Withdrawal request already processed",
      HttpStatus.BAD_REQUEST
  ),
  MINIMUM_WITHDRAWAL_NOT_MET(
      "MINIMUM_WITHDRAWAL_NOT_MET",
      "Minimum withdrawal amount not met",
      HttpStatus.BAD_REQUEST
  ),
  PENDING_WITHDRAWAL_EXISTS(
      "PENDING_WITHDRAWAL_EXISTS",
      "User already has a pending withdrawal request",
      HttpStatus.BAD_REQUEST
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
