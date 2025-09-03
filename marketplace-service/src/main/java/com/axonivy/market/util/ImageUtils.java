package com.axonivy.market.util;

import com.axonivy.market.controller.ImageController;
import com.axonivy.market.entity.ProductModuleContent;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.axonivy.market.constants.CommonConstants.IMAGE_ID_PREFIX;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

public class ImageUtils {
  public static final String IMAGE_ID_FORMAT_PATTERN = "imageId-\\w+";
  private static final Pattern PATTERN = Pattern.compile(IMAGE_ID_FORMAT_PATTERN);

  private ImageUtils() {
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
    content.forEach((key, value) -> {
      List<String> imageIds = extractAllImageIds(value);
      for (String imageId : imageIds) {
        String rawId = imageId.replace(IMAGE_ID_PREFIX, Strings.EMPTY);
        String imageLink;
        if (isProduction) {
          imageLink = createImageUrlForProduction(rawId);
        } else {
          imageLink = createImageUrl(rawId);
        }
        value = value.replace(imageId, imageLink);
      }
      content.put(key, value);
    });
  }

  private static List<String> extractAllImageIds(String content) {
    List<String> result = new ArrayList<>();
    var matcher = PATTERN.matcher(content);
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
    return linkTo(methodOn(ImageController.class).findImageById(imageId)).withSelfRel().getHref();
  }

  public static String createImageUrlForProduction(String imageId) {
    if (StringUtils.isBlank(imageId)) {
      return StringUtils.EMPTY;
    }
    return linkTo(methodOn(ImageController.class).findImageById(imageId)).toUriComponentsBuilder().build().getPath();
  }
}
