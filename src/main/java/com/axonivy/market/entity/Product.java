package com.axonivy.market.entity;

import com.axonivy.market.github.model.MavenArtifact;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    private String key;
    private String name;
    private String version;
    private String shortDescript;
    private String logoUrl;
    private Boolean listed;
    private String type;
    private List<String> tags;
    private String vendor;
    private String vendorImage;
    private String vendorUrl;
    private String platformReview;
    private String cost;
    private String sourceUrl;
    private String statusBadgeUrl;
    private String language;
    private String industry;
    private String compatibility;
    private Boolean validate;
    private Boolean contactUs;
    private Integer installationCount;
    private List<MavenArtifact> mavenArtifacts;
    private String repoUrl;
}