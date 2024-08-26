package com.axonivy.market.entity.productjsonfilecontent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

import static com.axonivy.market.constants.EntityConstants.PRODUCT_JSON_CONTENT;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(PRODUCT_JSON_CONTENT)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductJsonContent {
  @Id
  @JsonIgnore
  private String id;
  @JsonProperty("$schema")
  private String schema;
  @JsonIgnore
  private String tag;
  private String name;
  private List<Installer> installers;
  private Properties properties;
  private String minimumIvyVersion;
}
