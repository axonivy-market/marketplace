package com.axonivy.market.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.entity.Product;
import com.axonivy.market.model.ProductDetailModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class ProductDetailModelAssembler extends RepresentationModelAssemblerSupport<Product, ProductDetailModel> {

    private final ProductModelAssembler productModelAssembler;

    public ProductDetailModelAssembler(ProductModelAssembler productModelAssembler) {
        super(ProductDetailsController.class, ProductDetailModel.class);
        this.productModelAssembler = productModelAssembler;
    }

    @Override
    public ProductDetailModel toModel(Product product) {
        ProductDetailModel model = instantiateModel(product);
        productModelAssembler.createResource(model, product);
        model.add(linkTo(methodOn(ProductDetailsController.class).findProductDetails(product.getId(), product.getType()))
                .withSelfRel());
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
        model.setReadmeProductContents(product.getReadmeProductContents());
    }
}