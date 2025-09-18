package com.axonivy.market.factory;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.MetaConstants;
import com.axonivy.market.entity.Artifact;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.github.model.Meta;
import com.axonivy.market.model.DisplayValue;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHContent;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.axonivy.market.constants.CommonConstants.SLASH;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductFactory {

  private static final int TOKEN_LAST_SEGMENT = 1;
  private static final int TOKEN_SECOND_LAST_SEGMENT = 2;

  private static final ObjectMapper MAPPER = new ObjectMapper()
      .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  public static Product mappingByGHContent(Product product, GHContent content) {
    if (content == null) {
      return product;
    }

    var contentName = content.getName();
    if (StringUtils.endsWith(contentName, MetaConstants.META_FILE)) {
      mappingByMetaJSONFile(product, content);
    }
    return product;
  }

  public static Product mappingByMetaJSONFile(Product product, GHContent ghContent) {
    Meta meta;
    try {
      meta = jsonDecode(ghContent);
    } catch (IOException e) {
      log.error("Mapping from Meta file by GHContent failed", e);
      return product;
    }

    product.setId(meta.getId());
    product.setNames(mappingMultilingualismValueByMetaJSONFile(meta.getNames()));
    product.setMarketDirectory(extractParentDirectory(ghContent));
    product.setListed(meta.getListed());
    product.setType(meta.getType());
    product.setTags(meta.getTags());
    product.setVersion(meta.getVersion());
    product.setShortDescriptions(mappingMultilingualismValueByMetaJSONFile(meta.getDescriptions()));
    product.setVendor(StringUtils.defaultIfEmpty(meta.getVendor(), MetaConstants.DEFAULT_VENDOR_NAME));
    product.setVendorUrl(StringUtils.defaultIfEmpty(meta.getVendorUrl(), MetaConstants.DEFAULT_VENDOR_URL));
    product.setVendorImagePath(meta.getVendorImage());
    product.setVendorImageDarkModePath(meta.getVendorImageDarkMode());
    product.setPlatformReview(meta.getPlatformReview());
    product.setStatusBadgeUrl(meta.getStatusBadgeUrl());
    product.setLanguage(meta.getLanguage());
    product.setIndustry(meta.getIndustry());
    product.setContactUs(BooleanUtils.isTrue(meta.getContactUs()));
    product.setDeprecated(meta.getDeprecated());
    product.setCost(
        StringUtils.capitalize(StringUtils.defaultIfEmpty(meta.getCost(), MetaConstants.DEFAULT_COST_VALUE)));
    extractSourceUrl(product, meta);

    List<Artifact> artifacts;
    if (CollectionUtils.isEmpty(meta.getMavenArtifacts())) {
      artifacts = new ArrayList<>();
    } else {
      artifacts = meta.getMavenArtifacts();
    }

    for (Artifact artifact : artifacts) {
      artifact.setInvalidArtifact(!artifact.getArtifactId().contains(meta.getId()));
    }

    product.setArtifacts(artifacts);
    product.setReleasedVersions(new ArrayList<>());

    return product;
  }

  public static void transferComputedPersistedDataToProduct(Product persisted, Product product) {
    if (StringUtils.isNotEmpty(persisted.getMarketDirectory())) {
      product.setMarketDirectory(persisted.getMarketDirectory());
    }
    if (StringUtils.isNotEmpty(persisted.getLogoId())) {
      product.setLogoId(persisted.getLogoId());
    }
  }

  private static Map<String, String> mappingMultilingualismValueByMetaJSONFile(List<DisplayValue> list) {
    Map<String, String> value = new HashMap<>();
    if (!CollectionUtils.isEmpty(list)) {
      for (DisplayValue name : list) {
        value.put(name.getLocale(), name.getValue());
      }
    }
    return value;
  }

  private static String extractParentDirectory(GHContent ghContent) {
    var path = StringUtils.defaultIfEmpty(ghContent.getPath(), EMPTY);
    return path.replace(ghContent.getName(), EMPTY);
  }

  public static void extractSourceUrl(Product product, Meta meta) {
    var sourceUrl = meta.getSourceUrl();
    if (StringUtils.isBlank(sourceUrl)) {
      return;
    }
    String[] tokens = sourceUrl.split(SLASH);
    var tokensLength = tokens.length;
    var repositoryPath = sourceUrl;
    if (tokensLength > 1) {
      repositoryPath = String.join(SLASH, tokens[tokensLength - TOKEN_SECOND_LAST_SEGMENT],
          tokens[tokensLength - TOKEN_LAST_SEGMENT]);
    }
    product.setRepositoryName(repositoryPath);
    product.setSourceUrl(sourceUrl);
  }

  private static Meta jsonDecode(GHContent ghContent) throws IOException {
    return MAPPER.readValue(ghContent.read().readAllBytes(), Meta.class);
  }

  public static void mappingIdForProductModuleContent(ProductModuleContent content) {
    if (StringUtils.isNotBlank(content.getProductId()) && StringUtils.isNotBlank(content.getVersion())) {
      content.setId(
          String.format(CommonConstants.ID_WITH_NUMBER_PATTERN, content.getProductId(), content.getVersion()));
    }
  }

  public static void mappingIdForProductJsonContent(ProductJsonContent content) {
    if (StringUtils.isNotBlank(content.getProductId()) && StringUtils.isNotBlank(content.getVersion())) {
      content.setId(
          String.format(CommonConstants.ID_WITH_NUMBER_PATTERN, content.getProductId(), content.getVersion()));
    }
  }
}
