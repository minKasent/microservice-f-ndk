package com.ndk.identityservice.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "oauth2_clients")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OAuth2Client {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "client_id", unique = true, nullable = false, length = 100)
  private String clientId;

  @Column(name = "client_secret", nullable = false)
  private String clientSecret;

  @Column(name = "client_name", nullable = false, length = 200)
  private String clientName;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "oauth2_client_redirect_uris", joinColumns = @JoinColumn(name = "client_id"))
  @Column(name = "redirect_uri")
  private Set<String> redirectUris = new HashSet<>();

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "oauth2_client_post_logout_redirect_uris", joinColumns = @JoinColumn(name = "client_id"))
  @Column(name = "post_logout_redirect_uri")
  private Set<String> postLogoutRedirectUris = new HashSet<>();

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "oauth2_client_scopes", joinColumns = @JoinColumn(name = "client_id"))
  @Column(name = "scope")
  private Set<String> scopes = new HashSet<>();

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "oauth2_client_grant_types", joinColumns = @JoinColumn(name = "client_id"))
  @Column(name = "grant_type")
  private Set<String> grantTypes = new HashSet<>();

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "oauth2_client_auth_methods", joinColumns = @JoinColumn(name = "client_id"))
  @Column(name = "auth_method")
  private Set<String> authenticationMethods = new HashSet<>();

  @Column(name = "require_authorization_consent", nullable = false)
  private Boolean requireAuthorizationConsent = false;

  @Column(name = "require_proof_key", nullable = false)
  private Boolean requireProofKey = false;

  @Column(name = "access_token_time_to_live")
  private Integer accessTokenTimeToLive; // in seconds

  @Column(name = "refresh_token_time_to_live")
  private Integer refreshTokenTimeToLive; // in seconds

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by")
  private User createdBy;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

}