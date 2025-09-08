package com.axonivy.market.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

import java.io.Serial;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = PRODUCT_JSON_CONTENT)
public class ProductJsonContent extends AbstractAuditableEntity<String> {
  @Serial
  private static final long serialVersionUID = 1;

  @Id
  @JsonIgnore
  private String id;
  private String version;
  private String productId;
  private String name;
  @Column(columnDefinition = TEXT_TYPE)
  private String content;

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }
}
