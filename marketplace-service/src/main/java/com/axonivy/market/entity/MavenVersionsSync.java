package com.axonivy.market.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

import static com.axonivy.market.constants.EntityConstants.MAVEN_VERSION_SYNC;

@Getter
@Setter
@NoArgsConstructor
@Document(MAVEN_VERSION_SYNC)
public class MavenVersionsSync {
  @Id
  private String productId;
  private List<String> syncedTags;
  @LastModifiedDate
  private Date lastSync;
}
