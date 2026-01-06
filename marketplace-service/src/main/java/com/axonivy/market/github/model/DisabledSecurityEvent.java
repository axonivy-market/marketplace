package com.axonivy.market.github.model;

import com.axonivy.market.enums.AccessLevel;

public record DisabledSecurityEvent(String repoName,
                                    SecurityFeature feature,
                                    AccessLevel status) {
}
