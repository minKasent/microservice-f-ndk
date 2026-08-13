package com.ndk.contentservice.exception;

import com.ndk.common.api.exception.DevSharingExceptionInfo;
import org.springframework.http.HttpStatus;

public enum ExceptionEnum implements DevSharingExceptionInfo {
  // Content errors
  CONTENT_NOT_FOUND("CONTENT_NOT_FOUND", "Content not found", HttpStatus.NOT_FOUND),
  CONTENT_NOT_PUBLISHED("CONTENT_NOT_PUBLISHED", "Content is not published", HttpStatus.BAD_REQUEST),
  CONTENT_ALREADY_PURCHASED("CONTENT_ALREADY_PURCHASED", "Content already purchased", HttpStatus.BAD_REQUEST),
  UNAUTHORIZED_CONTENT_ACCESS("UNAUTHORIZED_CONTENT_ACCESS", "Unauthorized to access this content", HttpStatus.FORBIDDEN),
  
  // Category errors
  CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", "Category not found", HttpStatus.NOT_FOUND),
  
  // Chapter/Section errors
  CHAPTER_NOT_FOUND("CHAPTER_NOT_FOUND", "Chapter not found", HttpStatus.NOT_FOUND),
  SECTION_NOT_FOUND("SECTION_NOT_FOUND", "Section not found", HttpStatus.NOT_FOUND),
  
  // Moderator Review errors
  CONTENT_NOT_PENDING_REVIEW("CONTENT_NOT_PENDING_REVIEW", "Content is not pending review", HttpStatus.BAD_REQUEST),
  INVALID_REVIEW_ACTION("INVALID_REVIEW_ACTION", "Invalid review action", HttpStatus.BAD_REQUEST),
  
  // Authorization errors
  UNAUTHORIZED("UNAUTHORIZED", "Unauthorized access", HttpStatus.FORBIDDEN),
  
  // Content status errors
  CONTENT_CANNOT_BE_PUBLISHED("CONTENT_CANNOT_BE_PUBLISHED", "Content cannot be published in current status", HttpStatus.BAD_REQUEST),
  CONTENT_CANNOT_BE_SUBMITTED("CONTENT_CANNOT_BE_SUBMITTED", "Content cannot be submitted for review in current status", HttpStatus.BAD_REQUEST),
  
  // Block errors
  BLOCK_NOT_FOUND("BLOCK_NOT_FOUND", "Content block not found", HttpStatus.NOT_FOUND),
  BLOCK_NOT_BELONG_TO_CONTENT("BLOCK_NOT_BELONG_TO_CONTENT", "Block does not belong to this content", HttpStatus.BAD_REQUEST),
  PARENT_BLOCK_NOT_FOUND("PARENT_BLOCK_NOT_FOUND", "Parent block not found", HttpStatus.NOT_FOUND),
  PARENT_BLOCK_NOT_IN_SAME_CONTENT("PARENT_BLOCK_NOT_IN_SAME_CONTENT", "Parent block must be in the same content", HttpStatus.BAD_REQUEST),
  INVALID_BLOCK_POSITION("INVALID_BLOCK_POSITION", "Invalid block position", HttpStatus.BAD_REQUEST),
  CIRCULAR_PARENT_REFERENCE("CIRCULAR_PARENT_REFERENCE", "Cannot set block as its own parent or descendant", HttpStatus.BAD_REQUEST);

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
