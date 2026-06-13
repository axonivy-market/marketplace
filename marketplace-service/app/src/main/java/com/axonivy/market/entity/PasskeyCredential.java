package com.axonivy.market.entity;

import com.axonivy.market.core.entity.GenericIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.time.Instant;

import static com.axonivy.market.constants.EntityConstants.PASSKEY_CREDENTIAL;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = PASSKEY_CREDENTIAL)
public class PasskeyCredential extends GenericIdEntity {
  @Serial
  private static final long serialVersionUID = 1L;

  @Column(name = "github_user_id", nullable = false, unique = true)
  private String githubUserId;

  @Column(name = "credential_id", nullable = false, unique = true)
  private String credentialId;

  @Column(name = "user_handle", nullable = false)
  private String userHandle;

  @Column(name = "public_key_cose", nullable = false, columnDefinition = "TEXT")
  private String publicKeyCose;

  @Column(name = "signature_count", nullable = false)
  private long signatureCount;

  @Column(name = "credential_type", nullable = false, length = 64)
  private String credentialType;

  @Column(name = "uv_initialized", nullable = false)
  private boolean uvInitialized;

  @Column(name = "transports", columnDefinition = "TEXT")
  private String transports;

  @Column(name = "backup_eligible", nullable = false)
  private boolean backupEligible;

  @Column(name = "backup_state", nullable = false)
  private boolean backupState;

  @Column(name = "attestation_object", nullable = false, columnDefinition = "TEXT")
  private String attestationObject;

  @Column(name = "attestation_client_data_json", nullable = false, columnDefinition = "TEXT")
  private String attestationClientDataJson;

  @Column(name = "label", length = 255)
  private String label;

  @Column(name = "created", nullable = false)
  private Instant created;

  @Column(name = "last_used")
  private Instant lastUsed;
}
