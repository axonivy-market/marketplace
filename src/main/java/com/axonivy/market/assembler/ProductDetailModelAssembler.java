package com.axonivy.market.assembler;

import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.entity.Product;
import com.axonivy.market.model.ProductDetailModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Component
public class ProductDetailModelAssembler extends RepresentationModelAssemblerSupport<Product, ProductDetailModel> {

    public ProductDetailModelAssembler() {
        super(ProductDetailsController.class, ProductDetailModel.class);
    }

    @Override
    public ProductDetailModel toModel(Product product) {
        ProductDetailModel model = createModelWithId(product.getId(), product, product.getType());
        model.setId(product.getId());
        model.setName(product.getName());
        model.setShortDescript(product.getShortDescription());
        model.setType(StringUtils.capitalize(product.getType()));
        model.setTags(product.getTags());
        model.setLogoUrl(product.getLogoUrl());
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
        model.setShortDescript(product.getShortDescription());
        model.setCost(product.getCost());
        return model;
    }
}
