package com.axonivy.market.model;

import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.util.ImageUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static com.axonivy.market.constants.RequestMappingConstants.BEST_MATCH_BY_ID_AND_VERSION;
import static com.axonivy.market.constants.RequestMappingConstants.BY_ID_AND_VERSION;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Getter
@Setter
@NoArgsConstructor
public class ProductDetailModel extends ProductModel {
  @Schema(description = "Product vendor", example = "Axon Ivy AG")
  private String vendor;
  @Schema(description = "Product vendor url", example = "https://www.axonivy.com")
  private String vendorUrl;
  @Schema(description = "Product vendor image", example = "https://api.example.com/api/image/67079ca57b9ee74b16c18111")
  private String vendorImage;
  @Schema(description = "Product vendor image dark mode",
      example = "https://api.example.com/api/image/67079ca57b9ee74b16c18111")
  private String vendorImageDarkMode;
  @Schema(description = "Platform review", example = "4.5")
  private String platformReview;
  @Schema(description = "Latest release version from maven", example = "v10.0.25")
  private String newestReleaseVersion;
  @Schema(description = "Product cost", example = "Free")
  private String cost;
  @Schema(description = "Source repository url",
      example = "https://github.com/axonivy-market/adobe-acrobat-sign-connector")
  private String sourceUrl;
  @Schema(description = "Status badge url",
      example = "https://github.com/axonivy-market/adobe-acrobat-sign-connector/actions/workflows/ci.yml/badge.svg")
  private String statusBadgeUrl;
  @Schema(description = "Default language", example = "English")
  private String language;
  @Schema(description = "Product industry", example = "Cross-Industry")
  private String industry;
  @Schema(description = "Can contact us", example = "false")
  private Boolean contactUs;
  @Schema(description = "Is deprecated product", example = "false")
  private Boolean deprecated;
  private ProductModuleContent productModuleContent;
  @Schema(description = "Installation/download count", example = "0")
  private int installationCount;
  @Schema(description = "The api url to get metadata from product.json")
  private String metaProductJsonUrl;
  @Schema(description = "Compatibility", example = "10.0+")
  private String compatibilityRange;
  private boolean isMavenDropins;

  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(getId()).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || this.getClass() != obj.getClass()) {
      return false;
    }
    return new EqualsBuilder().append(getId(), ((ProductDetailModel) obj).getId()).isEquals();
  }

  public static ProductDetailModel createModel(Product product, boolean isProduction) {
    var model = new ProductDetailModel();
    ProductModel.createResource(model, product);
    createDetailResource(model, product, isProduction);
    return model;
  }

  public static void createDetailResource(ProductDetailModel model, Product product, boolean isProduction) {
    model.setVendor(product.getVendor());
    model.setVendorUrl(product.getVendorUrl());
    model.setNewestReleaseVersion(product.getNewestReleaseVersion());
    model.setPlatformReview(product.getPlatformReview());
    model.setSourceUrl(product.getSourceUrl());
    model.setStatusBadgeUrl(product.getStatusBadgeUrl());
    model.setLanguage(product.getLanguage());
    model.setIndustry(product.getIndustry());
    model.setContactUs(product.getContactUs());
    model.setDeprecated(product.getDeprecated());
    model.setCost(product.getCost());
    model.setInstallationCount(product.getInstallationCount());
    model.setCompatibilityRange(product.getCompatibilityRange());
    model.setProductModuleContent(
        ImageUtils.mappingImageForProductModuleContent(product.getProductModuleContent(), isProduction));
    if (StringUtils.isNotBlank(product.getVendorImage())) {
      if (isProduction) {
        model.setVendorImage(ImageUtils.createImageUrlForProduction(product.getVendorImage()));
      } else {
        model.setVendorImage(ImageUtils.createImageUrl(product.getVendorImage()));
      }
    }
    if (StringUtils.isNotBlank(product.getVendorImageDarkMode())) {
      if (isProduction) {
        model.setVendorImageDarkMode(ImageUtils.createImageUrlForProduction(product.getVendorImageDarkMode()));
      } else {
        model.setVendorImageDarkMode(ImageUtils.createImageUrl(product.getVendorImageDarkMode()));
      }
    }
    model.setMavenDropins(product.isMavenDropins());
  }

  public static void addModelLinks(ProductDetailModel model, Product product, String version, String path) {
    String productId = Optional.of(product).map(Product::getId).orElse(StringUtils.EMPTY);
    if (path.equals(BEST_MATCH_BY_ID_AND_VERSION)) {
      var link = linkTo(
          methodOn(ProductDetailsController.class).findProductJsonContent(productId,
              product.getBestMatchVersion(), version)).withSelfRel();
      model.setMetaProductJsonUrl(link.getHref());
    }
    model.add(getSelfLinkForProduct(productId, version, path));
  }

  public static Link getSelfLinkForProduct(String productId, String version, String path) {
    ResponseEntity<ProductDetailModel> selfLinkWithVersion;
    selfLinkWithVersion = switch (path) {
      case BEST_MATCH_BY_ID_AND_VERSION ->
          methodOn(ProductDetailsController.class).findBestMatchProductDetailsByVersion(productId, version);
      case BY_ID_AND_VERSION ->
          methodOn(ProductDetailsController.class).findProductDetailsByVersion(productId, version);
      default -> methodOn(ProductDetailsController.class).findProductDetails(productId, false);
    };
    return linkTo(selfLinkWithVersion).withSelfRel();
  }

}
