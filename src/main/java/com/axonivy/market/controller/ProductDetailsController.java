package com.axonivy.market.controller;

import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.service.VersionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.axonivy.market.assembler.ProductDetailModelAssembler;
import com.axonivy.market.model.ProductDetailModel;
import com.axonivy.market.service.ProductService;

import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT_DETAILS;

@RestController
@RequestMapping(PRODUCT_DETAILS)
public class ProductDetailsController {
	private final VersionService service;
	private final ProductService productService;
	private final ProductDetailModelAssembler detailModelAssembler;

	public ProductDetailsController(VersionService service, ProductService productService,
			ProductDetailModelAssembler detailModelAssembler) {
		this.service = service;
		this.productService = productService;
		this.detailModelAssembler = detailModelAssembler;
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProductDetailModel> findProductDetails(@PathVariable("id") String id,
			@RequestParam(name = "type") String type) {
		var productDetail = productService.fetchProductDetail(id, type);
		return new ResponseEntity<>(detailModelAssembler.toModel(productDetail), HttpStatus.OK);
	}

	@GetMapping("/{id}/versions")
	public ResponseEntity<List<MavenArtifactVersionModel>> findProductVersionsById(@PathVariable("id") String id,
			@RequestParam(name = "isShowDevVersion") boolean isShowDevVersion,
			@RequestParam(name = "designerVersion", required = false) String designerVersion) {
		List<MavenArtifactVersionModel> models = service.getArtifactsAndVersionToDisplay(id, isShowDevVersion,
				designerVersion);
		return new ResponseEntity<>(models, HttpStatus.OK);
	}
}
