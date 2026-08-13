package com.ndk.identityservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_profile")
public class UserProfile extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "birthday", nullable = false)
  private LocalDate birthday;

  @Column(name = "gender", nullable = false, length = 10)
  private String gender;

  @Column(name = "address", nullable = false)
  private String address;

  @Column(name = "bio", nullable = false)
  private String bio;

  @Column(name = "avartar", nullable = false)
  private String avartar;

  @Column(name = "company", nullable = false)
  private String company;

  @Column(name = "year_of_experience", nullable = false)
  private Byte yearOfExperience;

  @Column(name = "education_level", nullable = false)
  private Byte educationLevel;

  @Column(name = "linkedin", nullable = false)
  private String linkedin;

  @Column(name = "facebook", nullable = false)
  private String facebook;

  @Column(name = "personal_website", nullable = false)
  private String personalWebsite;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;
}
