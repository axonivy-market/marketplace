package com.axonivy.market.assembler;

import com.axonivy.market.constants.RequestMappingConstants;
import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.entity.Product;
import com.axonivy.market.model.ProductDetailModel;
import com.axonivy.market.util.VersionUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@Log4j2
public class ProductDetailModelAssembler extends RepresentationModelAssemblerSupport<Product, ProductDetailModel> {

  private final ProductModelAssembler productModelAssembler;

  public ProductDetailModelAssembler(ProductModelAssembler productModelAssembler) {
    super(ProductDetailsController.class, ProductDetailModel.class);
    this.productModelAssembler = productModelAssembler;
  }

  @Override
  public ProductDetailModel toModel(Product product) {
    return createModel(product, StringUtils.EMPTY, StringUtils.EMPTY);
  }

  public ProductDetailModel toModel(Product product, String requestPath) {
    return createModel(product, StringUtils.EMPTY, requestPath);
  }

  public ProductDetailModel toModel(Product product, String version, String requestPath) {
    return createModel(product, version, requestPath);
  }

  private ProductDetailModel createModel(Product product, String version, String requestPath) {
    ResponseEntity<ProductDetailModel> selfLinkWithTag;
    ProductDetailModel model = instantiateModel(product);
    productModelAssembler.createResource(model, product);
    String productId = Optional.of(product).map(Product::getId).orElse(StringUtils.EMPTY);

    if (requestPath.equals(RequestMappingConstants.BEST_MATCH_BY_ID_AND_VERSION)) {
      String bestMatchVersion = VersionUtils.getBestMatchVersion(product.getReleasedVersions(), version);
      Link link = linkTo(
          methodOn(ProductDetailsController.class).findProductJsonContent(productId, bestMatchVersion)).withSelfRel();
      model.setMetaProductJsonUrl(link.getHref());
    }

    selfLinkWithTag = switch (requestPath) {
      case RequestMappingConstants.BEST_MATCH_BY_ID_AND_VERSION ->
          methodOn(ProductDetailsController.class).findBestMatchProductDetailsByVersion(productId, version);
      case RequestMappingConstants.BY_ID_AND_VERSION ->
          methodOn(ProductDetailsController.class).findProductDetailsByVersion(productId, version);
      default -> methodOn(ProductDetailsController.class).findProductDetails(productId);
    };

    model.add(linkTo(selfLinkWithTag).withSelfRel());
    createDetailResource(model, product);
    return model;
  }

  private void createDetailResource(ProductDetailModel model, Product product) {
    model.setVendor(product.getVendor());
    model.setNewestReleaseVersion(product.getNewestReleaseVersion());
    model.setPlatformReview(product.getPlatformReview());
    model.setSourceUrl(product.getSourceUrl());
    model.setStatusBadgeUrl(product.getStatusBadgeUrl());
    model.setLanguage(product.getLanguage());
    model.setIndustry(product.getIndustry());
    model.setCompatibility(product.getCompatibility());
    model.setContactUs(product.getContactUs());
    model.setCost(product.getCost());
    model.setInstallationCount(product.getInstallationCount());
    model.setProductModuleContent(product.getProductModuleContent());
  }
}
