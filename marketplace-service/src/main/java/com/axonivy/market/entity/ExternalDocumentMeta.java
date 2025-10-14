package com.axonivy.market.entity;

import com.axonivy.market.enums.DocumentLanguage;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;

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
  @Enumerated(EnumType.STRING)
  private DocumentLanguage language;
}
