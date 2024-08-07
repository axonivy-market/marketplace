package com.axonivy.market.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductModuleContent implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;
  @Schema(description = "Target release tag", example = "v10.0.25")
  private String tag;
  @Schema(description = "Product detail description content ", example = "{ \"de\": \"E-Sign-Konnektor\", \"en\": \"E-sign connector\" }")
  private Map<String, String> description;
  @Schema(description = "Setup tab content", example = "Adobe Sign account creation: An Adobe Sign account needs to be created to setup and use the connector.")
  private String setup;
  @Schema(description = "Demo tab content", example = "The demo project can be used to test the authentication and signing and the demo implementation can be used as inspiration for development")
  private String demo;
  @Schema(description = "Is dependency artifact", example = "true")
  private Boolean isDependency;
  @Schema(example = "Adobe Acrobat Sign Connector")
  private String name;
  @Schema(description = "Product artifact's group id", example = "com.axonivy.connector.adobe.acrobat.sign")
  private String groupId;
  @Schema(description = "Product artifact's artifact id", example = "adobe-acrobat-sign-connector-product")
  private String artifactId;
  @Schema(description = "Artifact file type", example = "iar")
  private String type;
}
