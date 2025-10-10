package com.axonivy.market.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.axonivy.market.constants.EntityConstants.EXTERNAL_DOCUMENT_META;

import java.io.Serial;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = EXTERNAL_DOCUMENT_META)
public class ExternalDocumentMeta extends AuditableIdEntity {

  @Serial
  private static final long serialVersionUID = 1;

  private String productId;
  private String artifactId;
  private String artifactName;
  private String version;
  private String storageDirectory;
  private String relativeLink;
}
