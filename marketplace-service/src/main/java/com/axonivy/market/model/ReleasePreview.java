package com.axonivy.market.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Map;

import static com.axonivy.market.util.ProductContentUtils.DESCRIPTION;
import static com.axonivy.market.util.ProductContentUtils.DEMO;
import static com.axonivy.market.util.ProductContentUtils.SETUP;
import static com.axonivy.market.util.ProductContentUtils.replaceEmptyContentsWithEnContent;

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
