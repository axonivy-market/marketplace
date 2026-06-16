package com.axonivy.market.core.entity;

import static com.axonivy.market.core.constants.CoreEntityConstants.LANGUAGE;
import static com.axonivy.market.core.constants.CoreEntityConstants.NAME;
import static com.axonivy.market.core.constants.CoreEntityConstants.PRODUCT;
import static com.axonivy.market.core.constants.CoreEntityConstants.PRODUCT_DESCRIPTION;
import static com.axonivy.market.core.constants.CoreEntityConstants.PRODUCT_ID;
import static com.axonivy.market.core.constants.CoreEntityConstants.PRODUCT_NAME;
import static com.axonivy.market.core.constants.CoreEntityConstants.SHORT_DESCRIPTION;
import static com.axonivy.market.core.constants.CoreEntityConstants.TEXT_TYPE;
import static com.axonivy.market.core.constants.CorePostgresDBConstants.ID;

import java.io.Serial;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.axonivy.market.core.converter.StringListConverter;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = PRODUCT)
public class Product extends AbstractAuditableEntity<String> {
  @Serial
  private static final long serialVersionUID = 1;

  @Id
  private String id;  

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
  
  private String marketDirectory;
  private String logoUrl;
  private Boolean listed;
  private Boolean deprecated;
  private Boolean internal;
  private Boolean isArchived;
  private String type;
  private String vendor;
  private String vendorUrl;
  private String version;  
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
  private Date newestPublishedDate;
  private Date firstPublishedDate;
  private String newestReleaseVersion;
  private Boolean synchronizedInstallationCount;
  private String logoId;
  private String logoDarkId;
  
  @Transient
  private String vendorImagePath;
  @Transient
  private String vendorImageDarkModePath;
  @Transient
  private int installationCount;
  @Transient
  private ProductModuleContent productModuleContent;
  @Transient
  private String metaProductJsonUrl;
  @Transient
  private String bestMatchVersion;
  @Transient
  private boolean isMavenDropins;
  @Transient
  private String compatibilityRange;
  @Transient
  private Boolean isFocused;
  @Transient
  private String successor;
  @Transient
  private String alternativeExtension;

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
