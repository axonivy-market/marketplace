package com.axonivy.market.assembler;

import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.entity.Product;
import com.axonivy.market.model.ProductDetailModel;
import com.axonivy.market.model.ProductModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class ProductDetailModelAssembler extends RepresentationModelAssemblerSupport<Product, ProductDetailModel> {

    public ProductDetailModelAssembler() {
        super(ProductDetailsController.class, ProductDetailModel.class);
    }

    @Override
    public ProductDetailModel toModel(Product product) {
        ProductDetailModel model = createModelWithId(product.getKey(), product);
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
        model.setDescription(product.getDescription());
        model.setSetup(product.getSetup());
        model.setDemo(product.getDemo());
        return model;
    }
}
