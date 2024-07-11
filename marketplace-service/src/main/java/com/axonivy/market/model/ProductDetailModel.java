package com.axonivy.market.model;

import com.axonivy.market.entity.ProductModuleContent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Getter
@Setter
@NoArgsConstructor
public class ProductDetailModel extends ProductModel {
  private String vendor;
  private String platformReview;
  private String newestReleaseVersion;
  private String cost;
  private String sourceUrl;
  private String statusBadgeUrl;
  private String language;
  private String industry;
  private String compatibility;
  private Boolean contactUs;
  private ProductModuleContent productModuleContent;

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
