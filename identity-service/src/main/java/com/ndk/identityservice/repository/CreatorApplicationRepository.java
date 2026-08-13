package com.ndk.identityservice.repository;

import com.ndk.identityservice.entity.ApplicationStatus;
import com.ndk.identityservice.entity.CreatorApplication;
import com.ndk.identityservice.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CreatorApplicationRepository extends JpaRepository<CreatorApplication, Long> {

  Optional<CreatorApplication> findByUserAndStatus(User user, ApplicationStatus status);

  List<CreatorApplication> findByUser(User user);

  Page<CreatorApplication> findByStatus(ApplicationStatus status, Pageable pageable);

  @Query("SELECT ca FROM CreatorApplication ca WHERE ca.status = :status ORDER BY ca.createdAt ASC")
  Page<CreatorApplication> findPendingApplications(@Param("status") ApplicationStatus status,
      Pageable pageable);

  boolean existsByUserAndStatus(User user, ApplicationStatus status);
}
