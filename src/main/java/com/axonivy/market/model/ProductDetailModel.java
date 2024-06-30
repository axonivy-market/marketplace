package com.axonivy.market.model;

import com.axonivy.market.entity.ReadmeProductContent;
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
    private List<ReadmeProductContent> readmeProductContents;
}
