package com.axonivy.market.core;

import com.axonivy.market.core.constants.CoreMavenConstants;
import com.axonivy.market.core.entity.Artifact;
import com.axonivy.market.core.entity.MavenArtifactVersion;
import com.axonivy.market.core.entity.Metadata;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.entity.ProductDesignerInstallation;
import com.axonivy.market.core.entity.ProductJsonContent;
import com.axonivy.market.core.entity.ProductMarketplaceData;
import com.axonivy.market.core.entity.ProductModuleContent;
import com.axonivy.market.core.entity.key.MavenArtifactKey;
import com.axonivy.market.core.enums.Language;
import com.axonivy.market.core.enums.SortOption;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Log4j2
public class CoreBaseSetup {
  protected static final String MOCK_DOWNLOAD_URL = "https://maven.axonivy.com/com/axonivy/util/bpmn-statistic/10.0" +
      ".10/bpmn-statistic-10.0.10.zip";
  protected static final String MOCK_PRODUCT_ID = "bpmn-statistic";
  protected static final String MOCK_PRODUCT_NAME = "bpmn statistic";
  protected static final String MOCK_RELEASED_VERSION = "10.0.10";
  protected static final String SAMPLE_PRODUCT_ID = "amazon-comprehend";
  protected static final String SAMPLE_PRODUCT_NAME = "prody Comprehend";
  protected static final String MOCK_SPRINT_RELEASED_VERSION = "10.0.10-m123";
  protected static final String MOCK_SNAPSHOT_VERSION = "10.0.10-SNAPSHOT";
  protected static final String MOCK_BUGFIX_VERSION = "10.0.10.1";
  protected static final String MOCK_PRODUCT_ARTIFACT_ID = "bpmn-statistic-product";
  protected static final String MOCK_ARTIFACT_ID = "bpmn-statistic";
  protected static final String MOCK_GROUP_ID = "com.axonivy.util";
  protected static final String MOCK_ARTIFACT_NAME = "bpmn statistic (zip)";
  protected static final String INSTALLATION_FILE_PATH = "src/test/resources/installationCount.json";
  protected static final String LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME = "legacyInstallationCountPath";
  protected static final String MOCK_DESIGNER_VERSION = "12.0.4";
  protected static final String MOCK_PRODUCT_JSON_FILE_PATH = "src/test/resources/product.json";
  protected static final String MOCK_PRODUCT_JSON_WITH_DROPINS_FILE_PATH = "src/test/resources/product-dropins.json";
  protected static final Pageable PAGEABLE = PageRequest.of(0, 20,
      Sort.by(SortOption.ALPHABETICALLY.getOption()).descending());
  protected static final Pageable PAGEABLE_ALPHABETICALLY = PageRequest.of(0, 1,
      Sort.by(SortOption.ALPHABETICALLY.getOption()).descending());
  protected static final Pageable PAGEABLE_STANDARD = PageRequest.of(0, 20,
      Sort.by(SortOption.STANDARD.getOption()).descending());

  protected Page<Product> createPageProductsMock() {
    var mockProducts = new ArrayList<Product>();
    Map<String, String> name = new HashMap<>();
    Product mockProduct = new Product();
    mockProduct.setId(SAMPLE_PRODUCT_ID);
    name.put(Language.EN.getValue(), SAMPLE_PRODUCT_NAME);
    mockProduct.setNames(name);
    mockProduct.setType("connector");
    mockProduct.setMarketDirectory(SAMPLE_PRODUCT_ID);
    mockProduct.setReleasedVersions(List.of(MOCK_RELEASED_VERSION));
    mockProducts.add(mockProduct);

    mockProduct = new Product();
    mockProduct.setId("tel-search-ch-connector");
    name = new HashMap<>();
    name.put(Language.EN.getValue(), "Swiss phone directory");
    mockProduct.setNames(name);
    mockProduct.setType("util");
    mockProduct.setMarketDirectory(SAMPLE_PRODUCT_ID);
    mockProduct.setReleasedVersions(List.of(MOCK_RELEASED_VERSION));
    mockProducts.add(mockProduct);
    return new PageImpl<>(mockProducts);
  }

  protected MavenArtifactVersion mockAdditionalMavenArtifactVersion(String version, String artifactId) {
    MavenArtifactKey mavenArtifactKey = MavenArtifactKey.builder()
        .productVersion(version)
        .artifactId(artifactId)
        .isAdditionalVersion(true)
        .build();
    return MavenArtifactVersion.builder().id(mavenArtifactKey).downloadUrl(MOCK_DOWNLOAD_URL).build();
  }

  protected Metadata getMockMetadataWithVersions() {
    Metadata mockMetadata = getMockMetadata();
    mockMetadata.setRelease(MOCK_RELEASED_VERSION);
    mockMetadata.setLatest(MOCK_SPRINT_RELEASED_VERSION);
    mockMetadata.setVersions(
        Set.of(MOCK_SNAPSHOT_VERSION, MOCK_RELEASED_VERSION, MOCK_SPRINT_RELEASED_VERSION));
    return mockMetadata;
  }

  protected Metadata getMockMetadata() {
    return Metadata.builder().productId(MOCK_PRODUCT_ID).artifactId(MOCK_ARTIFACT_ID).groupId(
            MOCK_GROUP_ID).isProductArtifact(true).repoUrl(CoreMavenConstants.DEFAULT_IVY_MAVEN_BASE_URL).type(
            CoreMavenConstants.DEFAULT_PRODUCT_FOLDER_TYPE)
        .name(MOCK_ARTIFACT_NAME).build();
  }

  protected ProductMarketplaceData getMockProductMarketplaceData() {
    return ProductMarketplaceData.builder().id(MOCK_PRODUCT_ID).installationCount(3).build();
  }

  protected static ProductJsonContent getMockProductJsonContent() {
    ProductJsonContent result = new ProductJsonContent();
    result.setContent(getContentFromTestResourcePath(MOCK_PRODUCT_JSON_FILE_PATH));
    return result;
  }

  private static String getContentFromTestResourcePath(String path) {
    try {
      return Files.readString(Paths.get(path));
    } catch (IOException e) {
      log.warn("**Base Setup:: Can not get the test content from path {}", path);
      return StringUtils.EMPTY;
    }
  }

  protected List<ProductDesignerInstallation> createProductDesignerInstallationsMock() {
    var mockProductDesignerInstallations = new ArrayList<ProductDesignerInstallation>();
    com.axonivy.market.core.entity.ProductDesignerInstallation mockProductDesignerInstallation =
            new com.axonivy.market.core.entity.ProductDesignerInstallation();
    mockProductDesignerInstallation.setProductId(SAMPLE_PRODUCT_ID);
    mockProductDesignerInstallation.setDesignerVersion("10.0.22");
    mockProductDesignerInstallation.setInstallationCount(50);
    mockProductDesignerInstallations.add(mockProductDesignerInstallation);

    mockProductDesignerInstallation = new ProductDesignerInstallation();
    mockProductDesignerInstallation.setProductId(SAMPLE_PRODUCT_ID);
    mockProductDesignerInstallation.setDesignerVersion("11.4.0");
    mockProductDesignerInstallation.setInstallationCount(30);
    mockProductDesignerInstallations.add(mockProductDesignerInstallation);
    return mockProductDesignerInstallations;
  }

  protected ProductModuleContent getMockProductModuleContent() {
    ProductModuleContent productModuleContent = new ProductModuleContent();
    productModuleContent.setDescription(mockDescriptionForProductModuleContent());
    productModuleContent.setDemo(null);
    productModuleContent.setSetup(mockDescriptionForProductModuleContent());

    return productModuleContent;
  }

  private Map<String, String> mockDescriptionForProductModuleContent() {
    Map<String, String> mutableMap = new HashMap<>();
    mutableMap.put("en", "Login or create a new account.[demo-process](imageId-66e2b13c68f2f95b2f95548c)");
    mutableMap.put("de", "Login or create a new account.[demo-process](imageId-66e2b13c68f2f95b2f95548c)");
    return mutableMap;
  }

  protected Product getMockProduct() {
    Product mockProduct = Product.builder().id(MOCK_PRODUCT_ID).releasedVersions(new ArrayList<>()).artifacts(
        List.of(getMockArtifact(), getMockArtifact2())).build();
    mockProduct.getReleasedVersions().add(MOCK_RELEASED_VERSION);
    return mockProduct;
  }

  protected Artifact getMockArtifact() {
    Artifact mockArtifact = new Artifact();
    mockArtifact.setIsDependency(true);
    mockArtifact.setGroupId(MOCK_GROUP_ID);
    mockArtifact.setArtifactId(MOCK_ARTIFACT_ID);
    mockArtifact.setType("zip");
    mockArtifact.setName(MOCK_PRODUCT_NAME);
    return mockArtifact;
  }

  protected Artifact getMockArtifact2() {
    Artifact mockArtifact = new Artifact();
    mockArtifact.setIsDependency(true);
    mockArtifact.setGroupId(MOCK_GROUP_ID);
    mockArtifact.setArtifactId(MOCK_PRODUCT_ARTIFACT_ID);
    mockArtifact.setType("zip");
    mockArtifact.setName(MOCK_PRODUCT_NAME);
    return mockArtifact;
  }

  protected List<MavenArtifactVersion> getMockMavenArtifactVersion() {
    return new ArrayList<>();
  }

  protected MavenArtifactVersion mockMavenArtifactVersion(String version, String artifactId) {
    return mockMavenArtifactVersion(version, artifactId, "");
  }

  protected MavenArtifactVersion mockMavenArtifactVersion(String version, String artifactId, String downloadUrl) {
    MavenArtifactKey mavenArtifactKey = MavenArtifactKey.builder()
        .productVersion(version)
        .artifactId(artifactId)
        .build();

    return MavenArtifactVersion.builder().id(mavenArtifactKey).downloadUrl(downloadUrl).build();
  }

  protected List<MavenArtifactVersion> getMockMavenArtifactVersionWithData() {
    List<MavenArtifactVersion> mockMavenArtifactVersion = getMockMavenArtifactVersion();
    mockMavenArtifactVersion.add(mockMavenArtifactVersion(MOCK_SNAPSHOT_VERSION, null));
    return mockMavenArtifactVersion;
  }

  protected static ProductJsonContent getMockProductJsonContentContainMavenDropins() {
    ProductJsonContent result = new ProductJsonContent();
    result.setContent(getContentFromTestResourcePath(MOCK_PRODUCT_JSON_WITH_DROPINS_FILE_PATH));
    return result;
  }
}
