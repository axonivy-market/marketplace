package com.axonivy.market.entity;

import lombok.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;

import static com.axonivy.market.constants.EntityConstants.PRODUCT_DESIGNER_INSTALLATION;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(PRODUCT_DESIGNER_INSTALLATION)
public class ProductDesignerInstallation implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;
  @Id
  private String id;
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
