package com.axonivy.market.entity.productjsonfilecontent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Repository {
  private String id;
  private String url;
  private Snapshots snapshots;
}
