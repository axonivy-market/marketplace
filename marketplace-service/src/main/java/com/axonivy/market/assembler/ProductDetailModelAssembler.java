package com.axonivy.market.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.model.ProductDetailModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductDetailModelAssembler extends RepresentationModelAssemblerSupport<Product, ProductDetailModel> {

  private final ProductModelAssembler productModelAssembler;

  public ProductDetailModelAssembler(ProductModelAssembler productModelAssembler) {
    super(ProductDetailsController.class, ProductDetailModel.class);
    this.productModelAssembler = productModelAssembler;
  }

  @Override
  public ProductDetailModel toModel(Product product) {
    return createModel(product, null);
  }

  public ProductDetailModel toModel(Product product, String tag) {
    return createModel(product, tag);
  }

  private ProductDetailModel createModel(Product product, String tag) {
    ResponseEntity<ProductDetailModel> selfLinkWithTag;
    ProductDetailModel model = instantiateModel(product);
    productModelAssembler.createResource(model, product);
    if (StringUtils.isBlank(tag)) {
      selfLinkWithTag = methodOn(ProductDetailsController.class).findProductDetails(product.getId());
    } else {
      selfLinkWithTag = methodOn(ProductDetailsController.class).findProductDetailsByVersion(product.getId(), tag);
    }
    model.add(linkTo(selfLinkWithTag).withSelfRel());
    createDetailResource(model, product, tag);
    return model;
  }

  private void createDetailResource(ProductDetailModel model, Product product, String tag) {
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

    if (StringUtils.isBlank(tag) && StringUtils.isNotBlank(product.getNewestReleaseVersion())) {
      tag = product.getNewestReleaseVersion();
    }
    ProductModuleContent content = getProductModuleContentByTag(product.getProductModuleContents(), tag);
    model.setProductModuleContent(content);
  }

  private ProductModuleContent getProductModuleContentByTag(List<ProductModuleContent> contents, String tag) {
    return contents.stream().filter(content -> StringUtils.equals(content.getTag(), tag)).findAny().orElse(null);
  }
}
