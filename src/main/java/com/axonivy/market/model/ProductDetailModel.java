package com.axonivy.market.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Getter
@Setter
@NoArgsConstructor
public class ProductDetailModel extends ProductModel {
    private String vendor;
    private String vendorUrl;
    private String platformReview;
//    private String version;
    private String cost;
    private String sourceUrl;
    private String statusBadgeUrl;
    private String language;
    private String industry;
    private String compatibility;
//    private Boolean validate;
    private Boolean contactUs;

    private String content;
}
