package com.axonivy.market.model;

import com.axonivy.market.entity.ProductModuleContent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Getter
@Setter
@NoArgsConstructor
public class ProductDetailModel extends ProductModel {
  @Schema(description = "Product vendor", example = "Axon Ivy AG")
  private String vendor;
  @Schema(description = "Platform review", example = "4.5")
  private String platformReview;
  @Schema(description = "Latest release version from maven", example = "v10.0.25")
  private String newestReleaseVersion;
  @Schema(description = "Product cost", example = "Free")
  private String cost;
  @Schema(description = "Source repository url", example = "https://github.com/axonivy-market/adobe-acrobat-sign" +
          "-connector")
  private String sourceUrl;
  @Schema(description = "Status badge url", example = "https://github.com/axonivy-market/adobe-acrobat-sign-connector" +
          "/actions/workflows/ci.yml/badge.svg")
  private String statusBadgeUrl;
  @Schema(description = "Default language", example = "English")
  private String language;
  @Schema(description = "Product industry", example = "Cross-Industry")
  private String industry;
  @Schema(description = "Compatibility", example = "10.0+")
  private String compatibility;
  @Schema(description = "Can contact us", example = "false")
  private Boolean contactUs;
  private ProductModuleContent productModuleContent;
  @Schema(description = "Installation/download count", example = "0")
  private int installationCount;
  @Schema(description = "The api url to get metadata from product.json")
  private String metaProductJsonUrl;

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getId()).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || this.getClass() != obj.getClass()) {
      return false;
    }
    return new EqualsBuilder().append(getId(), ((ProductDetailModel) obj).getId()).isEquals();
  }
}
