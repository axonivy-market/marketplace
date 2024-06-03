package com.axonivy.market.factory;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHContent;

import com.axonivy.market.github.model.Meta;
import com.axonivy.market.model.Product;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ProductFactory {

  public static final String META_FILE = "meta.json";
  public static final String LOGO_FILE = "logo.png";

  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static Product mappingByGHContent(Product product, GHContent content) {
    var contentName = content.getName();
    try {
      if (contentName.endsWith(META_FILE)) {
        ProductFactory.mappingByMetaJson(product, content);
      }

      if (contentName.endsWith(LOGO_FILE)) {
        product.setLogoUrl(content.getDownloadUrl());
      }
    } catch (IOException e) {
      log.warn("Mapping from GHContent failed", e);
    }
    return product;
  }

  public static Product mappingByMetaJson(Product product, GHContent ghContent)
      throws StreamReadException, DatabindException, IOException {
    var meta = jsonDecode(ghContent);
    if (meta == null) {
      return product;
    }
    // log.warn(meta);
    product.setKey(meta.getId());
    product.setName(meta.getName());
    product.setListed(meta.getListed());
    product.setType(meta.getType());
    product.setTags(meta.getTags());
    product.setVersion(meta.getVersion());
    product.setShortDescript(meta.getDescription());
    product.setVendor(StringUtils.isBlank(meta.getVendor()) ? "Axon Ivy AG" : meta.getVendor());
    product.setVendorImage(
        StringUtils.isBlank(meta.getVendorImage()) ? "/images/misc/axonivy-logo-black.svg" : meta.getVendor());
    product.setVendorUrl(StringUtils.isBlank(meta.getVendorUrl()) ? "https://www.axonivy.com" : meta.getVendorUrl());
    product.setPlatformReview(StringUtils.isBlank(meta.getPlatformReview()) ? "4.0" : meta.getPlatformReview());
    product.setSourceUrl(meta.getSourceUrl());
    product.setStatusBadgeUrl(meta.getStatusBadgeUrl());
    product.setLanguage(meta.getLanguage());
    product.setIndustry(meta.getIndustry());
    product.setMavenArtifacts(meta.getMavenArtifacts());
    // TODO mapping default data
    // product.setCost() = ghContent->cost ?? 'Free';
    // product.setCompatibility(meta.get) = ghContent->compatibility ?? '';
    // product.setValidate(meta.get) = ghContent->validate ?? false;
    // product.setContactUs(meta) = ghContent->contactUs ?? false;
    return product;
  }

  public static Meta jsonDecode(GHContent ghContent) throws StreamReadException, DatabindException, IOException {
    if (ghContent != null) {
      log.warn(ghContent.getContent());
      return MAPPER.readValue(ghContent.read().readAllBytes(), Meta.class);
    }
    return null;
  }

  // public String getVersion()
  // {
  //   // if (empty($this->version)) {
  //   //   if ($this->getMavenProductInfo() != null) {
  //   //     $this->version = $this->getMavenProductInfo()->getNewestVersion() ?? '';
  //   //   }
  //   // }
  //   // return $this->version;
  //   if(StringUtils.isBlank(this.getVersion())) {

  //   }
  // }
}
