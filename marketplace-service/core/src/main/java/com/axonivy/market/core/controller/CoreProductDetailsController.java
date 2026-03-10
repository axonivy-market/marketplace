package com.axonivy.market.core.controller;

import static com.axonivy.market.core.constants.CoreRequestMappingConstants.PRODUCT_JSON_CONTENT_BY_PRODUCT_ID_AND_VERSION;

import static com.axonivy.market.core.constants.CoreRequestParamConstants.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@AllArgsConstructor
@Tag(name = "Product Detail Controllers", description = "API collection to get product's detail.")
public abstract class CoreProductDetailsController {

    @GetMapping(PRODUCT_JSON_CONTENT_BY_PRODUCT_ID_AND_VERSION)
    public ResponseEntity<Map<String, Object>> findProductJsonContent(
            @PathVariable(ID) String productId,
            @PathVariable(VERSION) String version,
            @RequestParam(name = DESIGNER_VERSION, required = false) String designerVersion) {

        Map<String, Object> productJsonContent =
                getProductJsonContent(productId, version, designerVersion);

        return ResponseEntity.ok(productJsonContent);
    }

    protected abstract Map<String, Object> getProductJsonContent(
            String productId,
            String version,
            String designerVersion);
}