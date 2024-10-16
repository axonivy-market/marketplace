package com.axonivy.market.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@Relation(collectionRelation = "products", itemRelation = "product")
@JsonInclude(Include.NON_NULL)
public class ProductModel extends RepresentationModel<ProductModel> {
  @Schema(description = "Product id", example = "jira-connector")
  private String id;
  @Schema(description = "Product name by locale",
      example = "{ \"de\": \"Atlassian Jira\", \"en\": \"Atlassian Jira\" }")
  private Map<String, String> names;
  @Schema(description = "Product's short descriptions by locale",
      example = "{ \"de\": \"Nutze den Jira Connector von Atlassian, um Jira-Tickets direkt von der Axon Ivy " +
              "Plattform aus zu verfolgen.\", \"en\": \"Atlassian's Jira connector lets you track issues directly " +
              "from the Axon Ivy platform\" }")
  private Map<String, String> shortDescriptions;
  @Schema(description = "Product's logo url",
      example = "https://api.example.com/api/image/67079ca57b9ee74b16c18111")
  private String logoUrl;
  @Schema(description = "Type of product", example = "connector")
  private String type;
  @Schema(description = "Tags of product", example = "[\"helper\"]")
  private List<String> tags;

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(id).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || this.getClass() != obj.getClass()) {
      return false;
    }
    return new EqualsBuilder().append(id, ((ProductModel) obj).getId()).isEquals();
  }
}
