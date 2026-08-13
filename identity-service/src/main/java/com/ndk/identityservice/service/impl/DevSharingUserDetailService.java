package com.ndk.identityservice.service.impl;

import com.ndk.identityservice.entity.User;
import com.ndk.identityservice.entity.UserRole;
import com.ndk.identityservice.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DevSharingUserDetailService implements UserDetailsService {
  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsernameWithRoles(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

    List<GrantedAuthority> authorities = user.getUserRoles() == null
        ? List.of()
        : user.getUserRoles().stream()
            .map(UserRole::getRole)
            .filter(role -> role != null && role.getRoleName() != null)
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName().name()))
            .collect(Collectors.toList());

    // Use userId as principal name so authentication.getName() returns userId (Ch11 pattern)
    return org.springframework.security.core.userdetails.User
        .withUsername(String.valueOf(user.getId()))
        .password(user.getPassword())
        .authorities(authorities)
        .accountExpired(false)
        .accountLocked(false)
        .credentialsExpired(false)
        .disabled(false)
        .build();
  }
}
