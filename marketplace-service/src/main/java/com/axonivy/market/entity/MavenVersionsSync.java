package com.axonivy.market.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;

import static com.axonivy.market.constants.EntityConstants.MAVEN_VERSION_SYNC;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(MAVEN_VERSION_SYNC)
public class MavenVersionsSync {
  @Id
  private String productId;
  private String artifactId;
  private String groupId;
  private String latestVersion;
  private String latestRelease;
  private LocalDateTime lastUpDated;
  @LastModifiedDate
  private Date lastSync;
}
