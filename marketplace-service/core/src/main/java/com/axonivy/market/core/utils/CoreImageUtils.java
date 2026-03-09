package com.axonivy.market.core.utils;

import com.axonivy.market.core.controller.CoreImageController;
import com.axonivy.market.core.entity.ProductModuleContent;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.axonivy.market.core.constants.CoreCommonConstants.IMAGE_ID_PREFIX;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class CoreImageUtils {
  public static final String IMAGE_ID_FORMAT_REGEX = "imageId-\\w+";
  private static final Pattern IMAGE_ID_FORMAT_PATTERN = Pattern.compile(IMAGE_ID_FORMAT_REGEX,
      Pattern.UNICODE_CHARACTER_CLASS);

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
    return linkTo(methodOn(CoreImageController.class).findImageById(imageId)).withSelfRel().getHref();
  }

  public static String createImageUrlForProduction(String imageId) {
    if (StringUtils.isBlank(imageId)) {
      return StringUtils.EMPTY;
    }
    return linkTo(methodOn(CoreImageController.class).findImageById(imageId)).toUriComponentsBuilder().build().getPath();
  }
}
