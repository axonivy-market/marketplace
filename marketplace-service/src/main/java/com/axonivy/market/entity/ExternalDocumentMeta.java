package com.axonivy.market.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

import static com.axonivy.market.constants.EntityConstants.EXTERNAL_DOCUMENT_META;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(EXTERNAL_DOCUMENT_META)
public class ExternalDocumentMeta {
  @Id
  private String id;
  private String productId;
  private String version;
  private String storageDirectory;
  private String relativeLink;
  @CreatedDate
  private Date createdAt;
  @LastModifiedDate
  private Date modifiedAt;
}
