package com.axonivy.market.controller;

import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.service.VersionService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.List;

import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT_DETAILS;

@RestController
@RequestMapping(PRODUCT_DETAILS)
public class ProductDetailsController {
    private final VersionService service;

    public ProductDetailsController(VersionService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    ResponseEntity<Object> findProduct(@PathVariable("id") String key,
                                              @RequestParam(name = "type", required = false) String type) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{id}/versions")
    public ResponseEntity<List<MavenArtifactVersionModel>> findProductVersionsById(@PathVariable("id") String id,
                                                                                   @RequestParam(name = "isShowDevVersion") boolean isShowDevVersion,
                                                                                   @RequestParam(name = "designerVersion", required = false) String designerVersion) {
        List<MavenArtifactVersionModel> models = service.getArtifactsAndVersionToDisplay(id, isShowDevVersion, designerVersion);
        return new ResponseEntity<>(models, HttpStatus.OK);
    }
}
