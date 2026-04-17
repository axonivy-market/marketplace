package com.axonivy.market.model;

import java.util.Date;

public interface ProductDeprecationProjection {
  String getId();
  Date getDeprecationDate();
  String getDeprecationRequester();
  Boolean getDeprecated();
}
