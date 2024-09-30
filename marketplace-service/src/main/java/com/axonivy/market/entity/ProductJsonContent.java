package com.axonivy.market.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Set;

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
  private String version;
  private String relatedTag;
  private String productId;
  private Set<String> relatedMavenVersions;
  private String name;
  private String content;
  @LastModifiedDate
  private Date updatedAt;
}