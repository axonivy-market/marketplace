package com.axonivy.market.assembler;

import com.axonivy.market.constants.RequestMappingConstants;
import com.axonivy.market.controller.ImageController;
import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.entity.Product;
import com.axonivy.market.model.ProductDetailModel;
import com.axonivy.market.util.ImageUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
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
    ResponseEntity<ProductDetailModel> selfLinkWithVersion;
    ProductDetailModel model = instantiateModel(product);
    productModelAssembler.createResource(model, product);
    String productId = Optional.of(product).map(Product::getId).orElse(StringUtils.EMPTY);

    if (requestPath.equals(RequestMappingConstants.BEST_MATCH_BY_ID_AND_VERSION)) {
      Link link = linkTo(
          methodOn(ProductDetailsController.class).findProductJsonContent(productId,
              product.getBestMatchVersion())).withSelfRel();
      model.setMetaProductJsonUrl(link.getHref());
    }

    selfLinkWithVersion = switch (requestPath) {
      case RequestMappingConstants.BEST_MATCH_BY_ID_AND_VERSION ->
          methodOn(ProductDetailsController.class).findBestMatchProductDetailsByVersion(productId, version);
      case RequestMappingConstants.BY_ID_AND_VERSION ->
          methodOn(ProductDetailsController.class).findProductDetailsByVersion(productId, version);
      default -> methodOn(ProductDetailsController.class).findProductDetails(productId, false);
    };

    model.add(linkTo(selfLinkWithVersion).withSelfRel());
    createDetailResource(model, product);
    return model;
  }

  private void createDetailResource(ProductDetailModel model, Product product) {
    model.setVendor(product.getVendor());
    model.setVendorUrl(product.getVendorUrl());
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
    model.setCompatibilityRange(product.getCompatibilityRange());
    model.setProductModuleContent(ImageUtils.mappingImageForProductModuleContent(product.getProductModuleContent()));
    if (StringUtils.isNotBlank(product.getVendorImage())) {
      Link vendorLink = linkTo(methodOn(ImageController.class).findImageById(product.getVendorImage())).withSelfRel();
      model.setVendorImage(vendorLink.getHref());
    }
    if (StringUtils.isNotBlank(product.getVendorImageDarkMode())) {
      Link vendorDarkModeLink =
          linkTo(methodOn(ImageController.class).findImageById(product.getVendorImageDarkMode())).withSelfRel();
      model.setVendorImageDarkMode(vendorDarkModeLink.getHref());
    }
    model.setMavenDropins(product.isMavenDropins());
  }
}
