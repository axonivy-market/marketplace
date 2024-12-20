package com.axonivy.market.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

import static com.axonivy.market.util.ProductContentUtils.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleasePreview {

  @Schema(description = "Product detail description content ",
      example = "{ \"de\": \"E-Sign-Konnektor\", \"en\": \"E-sign connector\" }")
  private Map<String, String> description;
  @Schema(description = "Setup tab content", example = "{ \"de\": \"Setup\", \"en\": \"Setup\" ")
  private Map<String, String> setup;
  @Schema(description = "Demo tab content", example = "{ \"de\": \"Demo\", \"en\": \"Demo\" ")
  private Map<String, String> demo;

  public static ReleasePreview from(Map<String, Map<String, String>> moduleContents) {
    return ReleasePreview.builder().description(replaceEmptyContentsWithEnContent(moduleContents.get(DESCRIPTION)))
        .demo(replaceEmptyContentsWithEnContent(moduleContents.get(DEMO)))
        .setup(replaceEmptyContentsWithEnContent(moduleContents.get(SETUP)))
        .build();
  }

}
