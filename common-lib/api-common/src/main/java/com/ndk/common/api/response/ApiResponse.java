package com.ndk.common.api.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
  private ResponseStatus status;
  private String message;
  private T data;
  private String errorCode;

  public static <T> ApiResponse<T> success(T data) {
    var response = new ApiResponse<T>();
    response.setStatus(ResponseStatus.SUCCESS);
    response.setData(data);
    return response;
  }

  public static <T> ApiResponse<T> error(T data, String message, String errorCode) {
    var response = new ApiResponse<T>();
    response.setStatus(ResponseStatus.ERROR);
    response.setData(data);
    response.setMessage(message);
    response.setErrorCode(errorCode);
    return response;
  }

  public static ApiResponse<?> error(String message, String errorCode) {
    var response = new ApiResponse<>();
    response.setStatus(ResponseStatus.ERROR);
    response.setMessage(message);
    response.setErrorCode(errorCode);
    return response;
  }
}