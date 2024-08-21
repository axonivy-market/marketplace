package com.axonivy.market.entity.productjsonfilecontent;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Repository {
  private String id;
  private String url;
  private Snapshots snapshots;
}
