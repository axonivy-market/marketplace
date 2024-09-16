package com.axonivy.market.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.Binary;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.axonivy.market.constants.EntityConstants.IMAGE;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(IMAGE)
public class Image {
  @Id
  private String id;
  @Schema(description = "Product id", example = "jira-connector")
  private String productId;
  @Schema(description = "The download url from github", example = "https://raw.githubusercontent.comamazon-comprehend/logo.png")
  private String imageUrl;
  @Schema(description = "The image content as binary type", example = "Binary(Buffer.from(\"89504e470d0a1a0a0000000d\", \"hex\"), 0)")
  private Binary imageData;
  @Schema(description = "The SHA from github", example = "93b1e2f1595d3a85e51b01")
  private String sha;
}
