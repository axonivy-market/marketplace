package com.axonivy.market.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;
import java.util.UUID;

import static com.axonivy.market.constants.EntityConstants.EXTERNAL_DOCUMENT_META;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = EXTERNAL_DOCUMENT_META)
@EntityListeners(AuditingEntityListener.class)
public class ExternalDocumentMeta {
  @Id
  private String id;
  private String productId;
  private String artifactId;
  private String artifactName;
  private String version;
  private String storageDirectory;
  private String relativeLink;
  @CreatedDate
  private Date createdAt;
  @LastModifiedDate
  private Date modifiedAt;

  @PrePersist
  private void ensureId() {
    if (this.id == null) {
      this.id = UUID.randomUUID().toString();
    }
  }
}
