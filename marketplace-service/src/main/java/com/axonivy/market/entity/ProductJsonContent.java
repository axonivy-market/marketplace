package com.axonivy.market.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.axonivy.market.constants.EntityConstants.PRODUCT_JSON_CONTENT;
import static com.axonivy.market.constants.EntityConstants.TEXT_TYPE;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = PRODUCT_JSON_CONTENT)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductJsonContent extends BaseEntity {
  @Id
  @JsonIgnore
  private String id;
  private String version;
  private String productId;
  private String name;
  @Column(columnDefinition = TEXT_TYPE)
  private String content;
}