package com.axonivy.market.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProductDetailModel extends ProductModel {
    private String vendor;
    private String vendorUrl;
    private String platformReview;
    private String newestReleaseVersion;
    private String cost;
    private String sourceUrl;
    private String statusBadgeUrl;
    private String language;
    private String industry;
    private String compatibility;
    private Boolean contactUs;
    private String description;
    private String setup;
    private String demo;
    private String key;
    private String name;
    private String shortDescript;
    private String logoUrl;
    private String type;
    private List<String> tags;
}
