package com.axonivy.market.core.utils;

import com.axonivy.market.core.builder.ImageLinkBuilder;
import static com.axonivy.market.core.constants.CoreCommonConstants.IMAGE_ID_PREFIX;
import com.axonivy.market.core.entity.ProductModuleContent;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class CoreImageUtils {
  public static final String IMAGE_ID_FORMAT_REGEX = "imageId-\\w+";
  private static final Pattern IMAGE_ID_FORMAT_PATTERN = Pattern.compile(IMAGE_ID_FORMAT_REGEX,
      Pattern.UNICODE_CHARACTER_CLASS);
  @Setter
  private static ImageLinkBuilder imageLinkBuilder;

  private CoreImageUtils() {
  }

  public static ProductModuleContent mappingImageForProductModuleContent(ProductModuleContent productModuleContent,
      boolean isProduction) {
    if (ObjectUtils.isEmpty(productModuleContent)) {
      return null;
    }
    mappingImageUrl(productModuleContent.getDescription(), isProduction);
    mappingImageUrl(productModuleContent.getDemo(), isProduction);
    mappingImageUrl(productModuleContent.getSetup(), isProduction);
    return productModuleContent;
  }

  private static void mappingImageUrl(Map<String, String> content, boolean isProduction) {
    if (ObjectUtils.isEmpty(content)) {
      return;
    }
    content.forEach((String key, String value) -> {
      List<String> imageIds = extractAllImageIds(value);
      for (String imageId : imageIds) {
        value = replaceImageIdWithImageLink(isProduction, value, imageId);
      }
      content.put(key, value);
    });
  }

  private static String replaceImageIdWithImageLink(boolean isProduction, String value, String imageId) {
    var rawId = imageId.replace(IMAGE_ID_PREFIX, Strings.EMPTY);
    String imageLink;
    if (isProduction) {
      imageLink = createImageUrlForProduction(rawId);
    } else {
      imageLink = createImageUrl(rawId);
    }
    value = value.replace(imageId, imageLink);
    return value;
  }


  private static List<String> extractAllImageIds(String content) {
    List<String> result = new ArrayList<>();
    var matcher = IMAGE_ID_FORMAT_PATTERN.matcher(content);
    while (matcher.find()) {
      var foundImgTag = matcher.group();
      result.add(foundImgTag);
    }
    return result;
  }

  public static String createImageUrl(String imageId) {
    if (StringUtils.isBlank(imageId)) {
      return StringUtils.EMPTY;
    }
    return imageLinkBuilder.createImageUrl(imageId);
  }

  public static String createImageUrlForProduction(String imageId) {
    if (StringUtils.isBlank(imageId)) {
      return StringUtils.EMPTY;
    }
    return imageLinkBuilder.createImageUrlForProduction(imageId);
  }
}
