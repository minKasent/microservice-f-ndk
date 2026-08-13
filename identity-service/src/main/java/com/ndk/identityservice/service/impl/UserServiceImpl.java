package com.ndk.identityservice.service.impl;

import com.ndk.common.api.logging.LogExecutionTime;

import com.ndk.common.api.exception.DevSharingException;
import com.ndk.identityservice.dto.response.UserDto;
import com.ndk.identityservice.entity.User;
import com.ndk.identityservice.exception.ExceptionEnum;
import com.ndk.identityservice.mapper.UserMapper;
import com.ndk.identityservice.repository.UserRepository;
import com.ndk.identityservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@LogExecutionTime
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final UserMapper userMapper;

  @Override
  @Transactional(readOnly = true)
  public UserDto getUserById(String userId) {
    log.info("Getting user by ID: {}", userId);

    long id;
    try {
      id = Long.parseLong(userId);
    } catch (NumberFormatException e) {
      throw new DevSharingException(ExceptionEnum.USER_NOT_FOUND_ERROR, new Object[]{userId});
    }

    User user = userRepository.findById(id)
        .orElseThrow(
            () -> new DevSharingException(ExceptionEnum.USER_NOT_FOUND_ERROR, new Object[]{userId}));

    return userMapper.toDto(user);
  }

  @Override
  @Transactional(readOnly = true)
  public UserDto getCurrentUser(String userId) {
    return getUserById(userId);
  }
}
