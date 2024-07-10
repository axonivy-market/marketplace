package com.axonivy.market.controller;

import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.service.ProductService;
import com.axonivy.market.service.VersionService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT_DETAILS;

@RestController
@RequestMapping(PRODUCT_DETAILS)
public class ProductDetailsController {
	private final VersionService versionService;
	private final ProductService productService;
	public ProductDetailsController(VersionService versionService, ProductService productService) {
		this.versionService = versionService;
		this.productService = productService;
	}

	@GetMapping("/{id}")
	public ResponseEntity<Object> findProduct(@PathVariable("id") String key,
			@RequestParam(name = "type", required = false) String type) {
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@GetMapping("/{id}/versions")
	public ResponseEntity<List<MavenArtifactVersionModel>> findProductVersionsById(@PathVariable("id") String id,
			@RequestParam(name = "isShowDevVersion") boolean isShowDevVersion,
			@RequestParam(name = "designerVersion", required = false) String designerVersion) {
		List<MavenArtifactVersionModel> models = versionService.getArtifactsAndVersionToDisplay(id, isShowDevVersion,
				designerVersion);
		return new ResponseEntity<>(models, HttpStatus.OK);
	}

	@Operation(summary = "increase installation count by 1", description = "increase installation count by 1")
	@PutMapping("/installationcount/{key}")
	public ResponseEntity<Integer> syncInstallationCount(@PathVariable("key") String key) {
		int result = productService.updateInstallationCountForProduct(key);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
}