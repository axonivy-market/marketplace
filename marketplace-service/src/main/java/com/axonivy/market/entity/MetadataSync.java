package com.axonivy.market.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.axonivy.market.constants.EntityConstants.MAVEN_METADATA_SYNC;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Document(MAVEN_METADATA_SYNC)
public class MetadataSync {
  @Id
  private String productId;
  private List<String> syncedVersions = new ArrayList<>();
  @LastModifiedDate
  private Date lastSync;
}
