package com.axonivy.market.entity;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.converter.StringListConverter;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.axonivy.market.constants.EntityConstants.PRODUCT;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = PRODUCT)
public class Product implements Serializable {
  @Serial
  private static final long serialVersionUID = -8770801877877277258L;
  @Id
  private String id;
  private String marketDirectory;

  @JsonProperty
  @ElementCollection
  @CollectionTable(name = "product_names", joinColumns = @JoinColumn(name = "product_id"))
  @MapKeyColumn(name = "language")
  @Column(name = "name", columnDefinition = "TEXT")
  private Map<String, String> names;

  @JsonProperty
  @ElementCollection
  @CollectionTable(name = "product_descriptions", joinColumns = @JoinColumn(name = "product_id"))
  @MapKeyColumn(name = "language")
  @Column(name = "shortDescription", columnDefinition = "TEXT")
  private Map<String, String> shortDescriptions;

  @Convert(converter = StringListConverter.class)
  @Column(name = "tags", nullable = false, columnDefinition = "TEXT")
  private List<String> tags;

  @Convert(converter = StringListConverter.class)
  @Column(name = "released_versions", nullable = false, columnDefinition = "TEXT")
  private List<String> releasedVersions;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @JsonManagedReference
  private List<Artifact> artifacts;

  private String logoUrl;
  private Boolean listed;
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
  private String compatibility;
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
  @LastModifiedDate
  private Date updatedAt;
  @Transient
  private String bestMatchVersion;
  @Transient
  private boolean isMavenDropins;
  @Transient
  private String compatibilityRange;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "id", referencedColumnName = "id") // Assuming IDs match in both tables
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
}
