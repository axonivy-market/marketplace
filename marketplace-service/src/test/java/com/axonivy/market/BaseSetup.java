package com.axonivy.market;

import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.*;
import com.axonivy.market.enums.Language;
import com.axonivy.market.enums.SortOption;
import com.axonivy.market.model.FeedbackApprovalModel;
import com.axonivy.market.entity.key.MavenArtifactKey;
import com.axonivy.market.model.VersionAndUrlModel;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.axonivy.market.constants.MavenConstants.DEFAULT_IVY_MIRROR_MAVEN_BASE_URL;

@Log4j2
public class BaseSetup {
  protected static final String AUTHORIZATION_HEADER = "Bearer valid_token";
  protected static final String ACCESS_TOKEN = "sampleAccessToken";
  protected static final String SAMPLE_PRODUCT_ID = "amazon-comprehend";
  protected static final String SAMPLE_PRODUCT_PATH = "/market/connector/amazon-comprehend";
  protected static final String SAMPLE_PRODUCT_NAME = "prody Comprehend";
  protected static final String SAMPLE_PRODUCT_REPOSITORY_NAME = "axonivy-market/amazon-comprehend";
  protected static final Pageable PAGEABLE_ALPHABETICALLY = PageRequest.of(0, 1,
      Sort.by(SortOption.ALPHABETICALLY.getOption()).descending());
  protected static final Pageable PAGEABLE_STANDARD = PageRequest.of(0, 20,
      Sort.by(SortOption.STANDARD.getOption()).descending());
  protected static final String MOCK_PRODUCT_ID = "bpmn-statistic";
  protected static final String MOCK_PRODUCT_ID_WITH_VERSION = "bpmn-statistic-10.0.10";
  protected static final String MOCK_ARTIFACT_ID = "bpmn-statistic";
  protected static final String MOCK_DEMO_ARTIFACT_ID = "bpmn-statistic-demo";
  protected static final String MOCK_PRODUCT_ARTIFACT_ID = "bpmn-statistic-product";
  protected static final String MOCK_RELEASED_VERSION = "10.0.10";
  protected static final String MOCK_SNAPSHOT_VERSION = "10.0.10-SNAPSHOT";
  protected static final String MOCK_DESIGNER_VERSION = "12.0.4";
  protected static final String MOCK_BUGFIX_VERSION = "10.0.10.1";
  protected static final String MOCK_SPRINT_RELEASED_VERSION = "10.0.10-m123";
  protected static final String MOCK_GROUP_ID = "com.axonivy.util";
  protected static final String MOCK_PRODUCT_NAME = "bpmn statistic";
  protected static final String MOCK_PRODUCT_REPOSITORY_NAME = "axonivy-market/bpmn-statistic";
  protected static final String MOCK_IMAGE_ID_FORMAT_1 = "imageId-66e2b14868f2f95b2f95549a";
  protected static final String MOCK_IMAGE_ID_FORMAT_2 = "imageId-66e2b14868f2f95b2f95550a";
  protected static final String MOCK_IMAGE_ID_FORMAT_3 = "imageId-66e2b14868f2f95b2f95551a";
  protected static final String MOCK_PRODUCT_JSON_FILE_PATH = "src/test/resources/product.json";
  protected static final String MOCK_PRODUCT_JSON_FILE_PATH_NO_URL = "src/test/resources/productMissingURL.json";
  protected static final String MOCK_PRODUCT_JSON_WITH_DROPINS_FILE_PATH = "src/test/resources/product-dropins.json";
  protected static final String MOCK_PRODUCT_JSON_DIR_PATH = "src/test/resources";
  protected static final String MOCK_PRODUCT_JSON_NODE_FILE_PATH = "src/test/resources/prouct-json-node.json";
  protected static final String MOCK_METADATA_FILE_PATH = "src/test/resources/metadata.xml";
  protected static final String MOCK_METADATA_FILE_PATH2 = "src/test/resources/metadata2.xml";
  protected static final String MOCK_METADATA_FILE_PATH3 = "src/test/resources/metadata3.xml";
  protected static final String MOCK_SNAPSHOT_METADATA_FILE_PATH = "src/test/resources/snapshotMetadata.xml";
  protected static final String MOCK_README_FILE = "src/test/resources/README.md";
  protected static final String MOCK_README_DE_FILE = "src/test/resources/README_DE.md";
  protected static final String MOCK_README_FILE_NO_DEMO_PART = "src/test/resources/README_NO_DEMO_PART.md";
  protected static final String MOCK_README_FILE_NO_SETUP_PART = "src/test/resources/README_NO_SETUP_PART.md";
  protected static final String MOCK_README_FILE_SWAP_DEMO_SETUP_PARTS = "src/test/resources/README_SWAP_DEMO_SETUP.md";
  protected static final String INVALID_FILE_PATH = "test/file/path";
  protected static final String MOCK_MAVEN_URL = DEFAULT_IVY_MIRROR_MAVEN_BASE_URL + "/com/axonivy/util/bpmn-statistic/maven" +
      "-metadata.xml";
  protected static final String MOCK_SNAPSHOT_MAVEN_URL = DEFAULT_IVY_MIRROR_MAVEN_BASE_URL + "/com/axonivy/util/bpmn-statistic" +
      "/10.0.10-SNAPSHOT/maven-metadata.xml";
  protected static final String MOCK_DOWNLOAD_URL = "https://maven.axonivy.com/com/axonivy/util/bpmn-statistic/10.0" +
      ".10/bpmn-statistic-10.0.10.zip";
  protected static final String MOCK_DUMP_DOWNLOAD_URL = "octopus.com";
  protected static final String MOCK_ARTIFACT_NAME = "bpmn statistic (zip)";
  protected static final String MOCK_ARTIFACT_DOWNLOAD_FILE = "bpmn-statistic.zip";
  protected static final String LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME = "legacyInstallationCountPath";
  protected static final String MOCK_IMAGE_URL = "https://raw.githubusercontent" +
      ".com/amazon-comprehend-connector-product/images/comprehend-demo-sentiment.png";
  protected static final String INSTALLATION_FILE_PATH = "src/test/resources/installationCount.json";
  protected static final String IMAGE_NAME = "test.png";
  protected static final String SAMPLE_LOGO_ID = "1234";

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

  protected List<ProductDesignerInstallation> createProductDesignerInstallationsMock() {
    var mockProductDesignerInstallations = new ArrayList<ProductDesignerInstallation>();
    ProductDesignerInstallation mockProductDesignerInstallation = new ProductDesignerInstallation();
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

  protected static String getMockProductJsonNodeContent() {
    return getContentFromTestResourcePath(MOCK_PRODUCT_JSON_NODE_FILE_PATH);
  }

  protected static String getMockReadmeContent() {
    return getContentFromTestResourcePath(MOCK_README_FILE);
  }

  protected static String getMockReadmeContent(String filePath) {
    if (StringUtils.isBlank(filePath)) {
      return getMockReadmeContent();
    }

    return getContentFromTestResourcePath(filePath);
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

  public static Image getMockImage() {
    Image image = new Image();
    image.setId("66e2b14868f2f95b2f95549a");
    image.setSha("914d9b6956db7a1404622f14265e435f36db81fa");
    image.setProductId(SAMPLE_PRODUCT_ID);
    image.setImageUrl(MOCK_IMAGE_URL);
    return image;
  }

  public static Image getMockImage2() {
    Image image = new Image();
    image.setId("66e2b14868f2f95b2f95550a");
    image.setSha("914d9b6956db7a1404622f14265e435f36db81fa");
    image.setProductId(SAMPLE_PRODUCT_ID);
    image.setImageUrl(MOCK_IMAGE_URL);
    return image;
  }

  protected String getMockSnapShotMetadataContent() {
    return getContentFromTestResourcePath(MOCK_SNAPSHOT_METADATA_FILE_PATH);
  }

  protected String getMockMetadataContent() {
    return getContentFromTestResourcePath(MOCK_METADATA_FILE_PATH);
  }

  protected String getMockMetadataContent2() {
    return getContentFromTestResourcePath(MOCK_METADATA_FILE_PATH2);
  }

  protected String getMockMetadataContent3() {
    return getContentFromTestResourcePath(MOCK_METADATA_FILE_PATH3);
  }

  protected Metadata buildMockMetadata() {
    return Metadata.builder().url(
        MOCK_MAVEN_URL).repoUrl(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL).groupId(MOCK_GROUP_ID).artifactId(
        MOCK_ARTIFACT_ID).type(MavenConstants.DEFAULT_PRODUCT_FOLDER_TYPE).productId(MOCK_PRODUCT_ID).build();
  }

  protected List<MavenArtifactVersion> getMockMavenArtifactVersion() {
    return new ArrayList<>();
  }

  protected List<MavenArtifactVersion> getMockMavenArtifactVersionWithData() {
    List<MavenArtifactVersion> mockMavenArtifactVersion = getMockMavenArtifactVersion();
    mockMavenArtifactVersion.add(mockMavenArtifactVersion(MOCK_SNAPSHOT_VERSION, null));
    return mockMavenArtifactVersion;
  }

  protected Product getMockProduct() {
    Product mockProduct = Product.builder().id(MOCK_PRODUCT_ID).releasedVersions(new ArrayList<>()).artifacts(
        List.of(getMockArtifact(), getMockArtifact2())).build();
    mockProduct.getReleasedVersions().add(MOCK_RELEASED_VERSION);
    return mockProduct;
  }

  protected List<Product> getMockProducts() {
    return List.of(getMockProduct());
  }

  protected Metadata getMockMetadata() {
    return Metadata.builder().productId(MOCK_PRODUCT_ID).artifactId(MOCK_ARTIFACT_ID).groupId(
        MOCK_GROUP_ID).isProductArtifact(true).repoUrl(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL).type(
        MavenConstants.DEFAULT_PRODUCT_FOLDER_TYPE).name(MOCK_ARTIFACT_NAME).build();
  }

  protected static InputStream getMockInputStream() {
    String jsonContent = getMockProductJsonContent().getContent();
    return new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));
  }

  protected static InputStream getMockProductJsonNodeContentInputStream() {
    String jsonContent = getContentFromTestResourcePath(MOCK_PRODUCT_JSON_FILE_PATH_NO_URL);
    return new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));
  }

  protected Metadata getMockMetadataWithVersions() {
    Metadata mockMetadata = getMockMetadata();
    mockMetadata.setRelease(MOCK_RELEASED_VERSION);
    mockMetadata.setLatest(MOCK_SPRINT_RELEASED_VERSION);
    mockMetadata.setVersions(
        Set.of(MOCK_SNAPSHOT_VERSION, MOCK_RELEASED_VERSION, MOCK_SPRINT_RELEASED_VERSION));
    return mockMetadata;
  }

  protected MavenArtifactVersion getMockMavenArtifactVersionWithDownloadUrl() {
    MavenArtifactKey key = MavenArtifactKey.builder().artifactId(MOCK_ARTIFACT_ID).build();
    return MavenArtifactVersion.builder().name(MOCK_PRODUCT_NAME)
        .id(key)
        .downloadUrl(MOCK_DOWNLOAD_URL).build();
  }

  protected static ProductJsonContent getMockProductJsonContentContainMavenDropins() {
    ProductJsonContent result = new ProductJsonContent();
    result.setContent(getContentFromTestResourcePath(MOCK_PRODUCT_JSON_WITH_DROPINS_FILE_PATH));
    return result;
  }

  protected ProductMarketplaceData getMockProductMarketplaceData() {
    return ProductMarketplaceData.builder().id(MOCK_PRODUCT_ID).installationCount(3).build();
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

  protected List<VersionAndUrlModel> mockVersionAndUrlModels() {
    VersionAndUrlModel versionAndUrlModel = VersionAndUrlModel.builder()
        .version("10.0.21")
        .url("/api/product-details/portal/10.0.21/json")
        .build();

    VersionAndUrlModel versionAndUrlModel2 = VersionAndUrlModel.builder()
        .version("10.0.22")
        .url("/api/product-details/portal/10.0.22/json")
        .build();

    return List.of(versionAndUrlModel, versionAndUrlModel2);
  }

  protected List<VersionAndUrlModel> mockVersionModels() {
    VersionAndUrlModel versionAndUrlModel = VersionAndUrlModel.builder()
        .version("11.3.1")
        .build();

    VersionAndUrlModel versionAndUrlModel2 = VersionAndUrlModel.builder()
        .version("10.0.22")
        .build();

    return List.of(versionAndUrlModel, versionAndUrlModel2);
  }

  protected List<VersionAndUrlModel> mockVersionModels2() {
    VersionAndUrlModel versionAndUrlModel = VersionAndUrlModel.builder()
        .version("11.3.2")
        .build();

    List<VersionAndUrlModel> versionAndUrlModels = new ArrayList<>(mockVersionModels());
    versionAndUrlModels.add(0, versionAndUrlModel);

    return versionAndUrlModels;
  }

  protected List<VersionAndUrlModel> mockVersionModels3() {
    VersionAndUrlModel versionAndUrlModel = VersionAndUrlModel.builder()
        .version("11.3.2")
        .build();

    VersionAndUrlModel versionAndUrlModel2 = VersionAndUrlModel.builder()
        .version("12.0.0")
        .build();

    List<VersionAndUrlModel> versionAndUrlModels = new ArrayList<>(mockVersionModels());
    versionAndUrlModels.add(0, versionAndUrlModel);
    versionAndUrlModels.add(0, versionAndUrlModel2);
    return versionAndUrlModels;
  }

  protected FeedbackApprovalModel mockFeedbackApproval() {
    return FeedbackApprovalModel.builder()
        .feedbackId("1")
        .moderatorName("Admin")
        .build();
  }

  protected ExternalDocumentMeta createExternalDocumentMock() {
    return ExternalDocumentMeta.builder()
        .relativeLink("/market-cache/portal/10.0.0/doc/index.html")
        .build();
  }

  protected MavenArtifactVersion mockMavenArtifactVersion(String version, String artifactId) {
    MavenArtifactKey mavenArtifactKey = MavenArtifactKey.builder()
        .productVersion(version)
        .artifactId(artifactId)
        .build();

    return MavenArtifactVersion.builder().id(mavenArtifactKey).downloadUrl("").build();
  }

  protected MavenArtifactVersion mockMavenArtifactVersion(String version, String artifactId, String downloadUrl) {
    MavenArtifactKey mavenArtifactKey = MavenArtifactKey.builder()
        .productVersion(version)
        .artifactId(artifactId)
        .build();

    return MavenArtifactVersion.builder().id(mavenArtifactKey).downloadUrl(downloadUrl).build();
  }

  protected MavenArtifactVersion mockAdditionalMavenArtifactVersion(String version, String artifactId) {
    MavenArtifactKey mavenArtifactKey = MavenArtifactKey.builder()
        .productVersion(version)
        .artifactId(artifactId)
        .isAdditionalVersion(true)
        .build();
    return MavenArtifactVersion.builder().id(mavenArtifactKey).downloadUrl(MOCK_DOWNLOAD_URL).build();
  }

  protected Set<ProductDependency> mockMavenDependencies(){
    ProductDependency mavenDependency = ProductDependency.builder()
        .artifactId(MOCK_DEMO_ARTIFACT_ID)
        .downloadUrl(MOCK_DOWNLOAD_URL)
        .version(MOCK_RELEASED_VERSION)
        .build();

    return Set.of(mavenDependency);
  }

  protected ProductDependency mockProductDependency(){
    return ProductDependency.builder()
        .productId(MOCK_PRODUCT_ID)
        .dependencies(mockMavenDependencies())
        .build();
  }

  protected ResponseEntity<Resource> getMockEntityResource() {
    Resource resource = getMockResource();
    return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment").contentType(
        MediaType.TEXT_PLAIN).body(resource);
  }

  protected static Resource getMockResource() {
    byte[] data = SAMPLE_LOGO_ID.getBytes(StandardCharsets.UTF_8);
    return new ByteArrayResource(data) {
      @Override
      public String getFilename() {
        return MOCK_ARTIFACT_DOWNLOAD_FILE;
      }
    };
  }

  protected byte[] getMockBytes() {
    return "test-content".getBytes();
  }
}
