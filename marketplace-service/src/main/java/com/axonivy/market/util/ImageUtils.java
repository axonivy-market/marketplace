package com.axonivy.market.util;

import static com.axonivy.market.constants.CommonConstants.IMAGE_ID_PREFIX;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.axonivy.market.controller.ImageController;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.hateoas.Link;
import com.axonivy.market.entity.ProductModuleContent;

public class ImageUtils {
  public static final String IMAGE_ID_FORMAT_PATTERN = "imageId-\\w+";
  private static final Pattern PATTERN = Pattern.compile(IMAGE_ID_FORMAT_PATTERN);

  private ImageUtils() {
  }

  public static ProductModuleContent mappingImageForProductModuleContent(ProductModuleContent productModuleContent) {
    if (ObjectUtils.isEmpty(productModuleContent)) {
      return null;
    }
    mappingImageUrl(productModuleContent.getDescription());
    mappingImageUrl(productModuleContent.getDemo());
    mappingImageUrl(productModuleContent.getSetup());
    return productModuleContent;
  }

  private static void mappingImageUrl(Map<String, String> content) {
    if (ObjectUtils.isEmpty(content)) {
      return;
    }
    content.forEach((key, value) -> {
      List<String> imageIds = extractAllImageIds(value);
      for (String imageId : imageIds) {
        String rawId = imageId.replace(IMAGE_ID_PREFIX, Strings.EMPTY);
        Link link = linkTo(methodOn(ImageController.class).findImageById(rawId)).withSelfRel();
        value = value.replace(imageId, link.getHref());
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
}
