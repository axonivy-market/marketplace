package com.axonivy.market.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

import static com.axonivy.market.constants.EntityConstants.PRODUCT_JSON_CONTENT;

@Getter
@Setter
@AllArgsConstructor
@Document(PRODUCT_JSON_CONTENT)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductJsonContent {
  @Id
  @JsonIgnore
  private String id;
  private String version;
  private String productId;
  private String name;
  private String content;
  private Date createdAt = new Date();

  public ProductJsonContent() {
    this.createdAt = new Date();
  }
}
