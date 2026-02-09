package com.axonivy.market.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;

import static com.axonivy.market.constants.EntityConstants.PRODUCT_DESIGNER_INSTALLATION;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = PRODUCT_DESIGNER_INSTALLATION)
public class ProductDesignerInstallation extends AuditableIdEntity {
  @Serial
  private static final long serialVersionUID = 1;

  private String productId;
  private String designerVersion;
  private int installationCount;

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(productId).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || this.getClass() != obj.getClass()) {
      return false;
    }
    return new EqualsBuilder().append(productId, ((ProductDesignerInstallation) obj).getProductId()).isEquals();
  }
}
