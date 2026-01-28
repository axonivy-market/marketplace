package com.axonivy.market.core.utils;

import com.axonivy.market.core.constants.CoreMavenConstants;
import com.axonivy.market.core.entity.Metadata;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class CoreMavenUtils {
  public static boolean isProductMetadata(Metadata metadata) {
    return StringUtils.endsWith(Objects.requireNonNullElse(metadata, new Metadata()).getArtifactId(),
        CoreMavenConstants.PRODUCT_ARTIFACT_POSTFIX);
  }
}
