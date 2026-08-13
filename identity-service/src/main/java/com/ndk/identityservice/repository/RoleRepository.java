package com.ndk.identityservice.repository;

import com.ndk.identityservice.entity.Role;
import com.ndk.identityservice.entity.RoleEnum;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

  Optional<Role> findByRoleName(RoleEnum roleName);

}
