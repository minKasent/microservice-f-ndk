package com.ndk.purchase.exception;

import com.ndk.common.api.exception.DevSharingExceptionInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExceptionEnum implements DevSharingExceptionInfo {
  PURCHASE_NOT_FOUND("PURCHASE_001", "error.purchase.notFound", HttpStatus.NOT_FOUND),
  CONTENT_ALREADY_OWNED("PURCHASE_002", "error.purchase.contentAlreadyOwned", HttpStatus.CONFLICT),
  INSUFFICIENT_CREDIT("PURCHASE_003", "error.purchase.insufficientCredit", HttpStatus.BAD_REQUEST),
  PURCHASE_FAILED("PURCHASE_004", "error.purchase.failed", HttpStatus.INTERNAL_SERVER_ERROR),
  
  CONTENT_NOT_FOUND("CONTENT_001", "error.content.notFound", HttpStatus.NOT_FOUND),
  CONTENT_NOT_AVAILABLE("CONTENT_002", "error.content.notAvailable", HttpStatus.BAD_REQUEST),
  
  USER_NOT_FOUND("USER_001", "error.user.notFound", HttpStatus.NOT_FOUND),
  CREATOR_NOT_FOUND("USER_002", "error.creator.notFound", HttpStatus.NOT_FOUND),
  
  TRANSACTION_FAILED("TRANSACTION_001", "error.transaction.failed", HttpStatus.INTERNAL_SERVER_ERROR),
  CREDIT_DEDUCTION_FAILED("TRANSACTION_002", "error.transaction.deductFailed", HttpStatus.INTERNAL_SERVER_ERROR),
  COMMISSION_PAYMENT_FAILED("TRANSACTION_003", "error.transaction.addCommissionFailed", HttpStatus.INTERNAL_SERVER_ERROR),
  
  LIBRARY_NOT_FOUND("LIBRARY_001", "error.library.notFound", HttpStatus.NOT_FOUND);

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
