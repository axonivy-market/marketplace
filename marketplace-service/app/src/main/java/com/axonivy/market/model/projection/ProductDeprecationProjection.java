package com.axonivy.market.model.projection;

import java.util.Date;

public interface ProductDeprecationProjection {
  String getId();

  Date getDeprecationDate();

  String getDeprecationRequester();

  Boolean getDeprecated();

  Boolean getIsArchived();
}
