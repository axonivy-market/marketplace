package com.axonivy.market.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serial;
import java.io.Serializable;

import static com.axonivy.market.constants.EntityConstants.PRODUCT_MARKETPLACE_DATA;

@Getter
@Setter
@NoArgsConstructor
@Document(PRODUCT_MARKETPLACE_DATA)
public class ProductMarketplaceData implements Serializable {

  @Serial
  private static final long serialVersionUID = -8770801879877277456L;

  @Id
  private String id;
  private int installationCount;
  private Boolean synchronizedInstallationCount;
  private Integer customOrder;
}