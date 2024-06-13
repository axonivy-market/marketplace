package com.axonivy.market.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Transient;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class MavenArtifactModel {
    private String name;
    private String downloadUrl;
    @Transient
    private  Boolean isProductArtifact;
}
