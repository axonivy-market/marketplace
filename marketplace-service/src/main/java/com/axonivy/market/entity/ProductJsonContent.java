package com.axonivy.market.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

import static com.axonivy.market.constants.EntityConstants.PRODUCT_JSON_CONTENT;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = PRODUCT_JSON_CONTENT)
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductJsonContent {
  @Id
  @JsonIgnore
  private String id;
  private String version;
  private String productId;
  private String name;
  @Column(columnDefinition = "TEXT")
  private String content;
  @LastModifiedDate
  private Date updatedAt;
}