package com.axonivy.market.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.axonivy.market.constants.EntityConstants.BYTEA_TYPE;
import static com.axonivy.market.constants.EntityConstants.IMAGE;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = IMAGE)
public class Image extends GenericIdEntity {

  @Schema(description = "Product id", example = "jira-connector")
  private String productId;
  @Schema(description = "The download url from github",
      example = "https://raw.githubusercontent.comamazon-comprehend/logo.png")
  private String imageUrl;

  @Schema(description = "The image content as byte array")
  @Column(columnDefinition = BYTEA_TYPE)
  private byte[] imageData;

  @Schema(description = "The SHA from github", example = "93b1e2f1595d3a85e51b01")
  private String sha;

}
