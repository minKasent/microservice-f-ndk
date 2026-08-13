package com.ndk.identityservice.service;

import com.ndk.identityservice.dto.response.UserDto;

public interface UserService {

  UserDto getUserById(String userId);

  UserDto getCurrentUser(String userId);
}
