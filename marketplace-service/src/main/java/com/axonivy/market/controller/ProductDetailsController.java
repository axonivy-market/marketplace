package com.axonivy.market.controller;

import com.axonivy.market.assembler.ProductModelAssembler;
import com.axonivy.market.entity.Product;
import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.model.ProductModel;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.VersionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final ProductRepository productRepository;
    private final ProductModelAssembler assembler;

	public ProductDetailsController(ProductRepository productRepository, VersionService service, ProductModelAssembler assembler) {
        this.productRepository = productRepository;
        this.service = service;
        this.assembler = assembler;
	}

    @GetMapping("/{id}")
    public ResponseEntity<ProductModel> findProduct(@PathVariable("id") String id,
                                                    @RequestParam(name = "type", required = false) String type) {
        Product product = productRepository.findById(id).orElse(null);
        return new ResponseEntity<>(assembler.toModel(product), HttpStatus.OK);
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