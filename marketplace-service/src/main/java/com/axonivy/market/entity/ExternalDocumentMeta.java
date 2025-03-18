package com.axonivy.market.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.axonivy.market.constants.EntityConstants.EXTERNAL_DOCUMENT_META;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = EXTERNAL_DOCUMENT_META)
public class ExternalDocumentMeta extends AuditableEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;
  private String productId;
  private String artifactId;
  private String artifactName;
  private String version;
  private String storageDirectory;
  private String relativeLink;
}
