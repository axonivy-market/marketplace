package com.axonivy.market.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.axonivy.market.constants.EntityConstants.PRODUCT_MARKETPLACE_DATA;

import java.io.Serial;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = PRODUCT_MARKETPLACE_DATA)
public class ProductMarketplaceData extends AbstractGenericEntity<String> {
  @Serial
  private static final long serialVersionUID = 1;

  @Id
  private String id;
  private int installationCount;
  private Boolean synchronizedInstallationCount;
  private Integer customOrder;

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }
}
