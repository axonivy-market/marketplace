package com.axonivy.market.core.utils;

import com.axonivy.market.core.constants.CoreMavenConstants;
import com.axonivy.market.core.entity.Metadata;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CoreMavenUtils {
  public static boolean isProductMetadata(Metadata metadata) {
    return StringUtils.endsWith(Objects.requireNonNullElse(metadata, new Metadata()).getArtifactId(),
        CoreMavenConstants.PRODUCT_ARTIFACT_POSTFIX);
  }
}
