package com.axonivy.market.entity;

import com.axonivy.market.converter.StringListConverter;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Transient;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.axonivy.market.constants.EntityConstants.*;
import static com.axonivy.market.constants.PostgresDBConstants.ID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = PRODUCT)
public class Product extends AuditableEntity<String> {

  @Id
  private String id;
  private String marketDirectory;

  @JsonProperty
  @ElementCollection
  @CollectionTable(name = PRODUCT_NAME, joinColumns = @JoinColumn(name = PRODUCT_ID))
  @MapKeyColumn(name = LANGUAGE)
  @Column(name = NAME, columnDefinition = TEXT_TYPE)
  private Map<String, String> names;

  @JsonProperty
  @ElementCollection
  @CollectionTable(name = PRODUCT_DESCRIPTION, joinColumns = @JoinColumn(name = PRODUCT_ID))
  @MapKeyColumn(name = LANGUAGE)
  @Column(name = SHORT_DESCRIPTION, columnDefinition = TEXT_TYPE)
  private Map<String, String> shortDescriptions;

  @Convert(converter = StringListConverter.class)
  @Column(nullable = false, columnDefinition = TEXT_TYPE)
  private List<String> tags;

  @Convert(converter = StringListConverter.class)
  @Column(nullable = false, columnDefinition = TEXT_TYPE)
  private List<String> releasedVersions;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
  private List<Artifact> artifacts;

  private String logoUrl;
  private Boolean listed;
  private Boolean deprecated;
  private String type;
  private String vendor;
  private String vendorUrl;
  private String version;
  @Transient
  private String vendorImagePath;
  @Transient
  private String vendorImageDarkModePath;
  private String vendorImage;
  private String vendorImageDarkMode;
  private String platformReview;
  private String cost;
  private String repositoryName;
  private String sourceUrl;
  private String statusBadgeUrl;
  private String language;
  private String industry;
  private Boolean validate;
  private Boolean contactUs;
  @Transient
  private int installationCount;
  private Date newestPublishedDate;
  private Date firstPublishedDate;
  private String newestReleaseVersion;
  @Transient
  private ProductModuleContent productModuleContent;
  private Boolean synchronizedInstallationCount;
  @Transient
  private String metaProductJsonUrl;
  private String logoId;
  @Transient
  private String bestMatchVersion;
  @Transient
  private boolean isMavenDropins;
  @Transient
  private String compatibilityRange;

  @OneToOne
  @JoinColumn(name = ID, referencedColumnName = ID)
  private ProductMarketplaceData productMarketplaceData;

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

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }
}
