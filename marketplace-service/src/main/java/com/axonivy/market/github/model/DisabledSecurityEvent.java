package com.axonivy.market.github.model;

import com.axonivy.market.enums.AccessLevel;
import com.axonivy.market.enums.SecurityFeature;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DisabledSecurityEvent {
    private String repoName;
    private SecurityFeature feature;
    private AccessLevel status;
}
