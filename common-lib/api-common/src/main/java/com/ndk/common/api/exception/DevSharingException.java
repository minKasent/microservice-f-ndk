package com.ndk.common.api.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DevSharingException extends RuntimeException {
  private DevSharingExceptionInfo exceptionInfo;
  private Object[] args;

}
