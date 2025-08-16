package com.axonivy.market.util;

import com.axonivy.market.controller.ImageController;
import com.axonivy.market.entity.ProductModuleContent;
import org.apache.commons.lang3.ObjectUtils;
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
        value = value.replace(imageId, createImageUrl(rawId, isProduction));
      }
      content.put(key, value);
    });
  }

  private static List<String> extractAllImageIds(String content) {
    List<String> result = new ArrayList<>();
    Matcher matcher = PATTERN.matcher(content);
    while (matcher.find()) {
      var foundImgTag = matcher.group();
      result.add(foundImgTag);
    }
    return result;
  }

  public static String createImageUrl(String imageId, boolean isProduction) {
    var imageWebLink = linkTo(methodOn(ImageController.class).findImageById(imageId));
    var link = imageWebLink.withSelfRel().getHref();
    if (isProduction) {
      link = imageWebLink.toUriComponentsBuilder().build().getPath();
    }
    return ObjectUtils.isEmpty(link) ? imageId : link;
  }
}
