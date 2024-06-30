package com.axonivy.market.entity;

import static com.axonivy.market.constants.EntityConstants.PRODUCT;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(PRODUCT)
public class Product implements Serializable {

  private static final long serialVersionUID = -8770801877877277258L;
  @Id
  private String id;
  private String marketDirectory;
  private String name;
  private String version;
  private String shortDescription;
  private String logoUrl;
  private Boolean listed;
  private String type;
  private List<String> tags;
  private String vendor;
  private String vendorUrl;
  private String platformReview;
  private String cost;
  private String repositoryName;
  private String sourceUrl;
  private String statusBadgeUrl;
  private String language;
  private String industry;
  private String compatibility;
  private Boolean validate;
  private Boolean contactUs;
  private Integer installationCount;
  private Date newestPublishedDate;
  private String newestReleaseVersion;
  private List<ReadmeProductContent> readmeProductContents;

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(id).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || this.getClass() != obj.getClass()) {
      return false;
    }
    return new EqualsBuilder().append(id, ((Product) obj).getId()).isEquals();
  }

}