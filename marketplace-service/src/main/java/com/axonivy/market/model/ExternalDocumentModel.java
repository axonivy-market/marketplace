package com.axonivy.market.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.hateoas.RepresentationModel;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalDocumentModel extends RepresentationModel<ExternalDocumentModel> {
  @Schema(description = "Product id", example = "portal")
  private String productId;
  @Schema(description = "Name of artifact", example = "Portal Guide")
  private String artifactName;
  @Schema(description = "Version of artifact", example = "10.0.0")
  private String version;
  @Schema(description = "Relative link of document page",
      example = "/market-cache/portal/portal-guide/10.0.12/doc/index.html")
  private String relativeLink;

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(productId).append(version).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || this.getClass() != obj.getClass()) {
      return false;
    }
    var compareExternalDocumentModel = (ExternalDocumentModel) obj;
    return new EqualsBuilder().append(productId, compareExternalDocumentModel.getProductId())
        .append(version, compareExternalDocumentModel.getVersion()).isEquals();
  }
}
