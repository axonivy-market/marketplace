package com.axonivy.market;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductDesignerInstallation;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.enums.Language;
import com.axonivy.market.enums.SortOption;
import com.axonivy.market.model.MavenArtifactModel;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Log4j2
public class BaseSetup {
  protected static final String SAMPLE_PRODUCT_ID = "amazon-comprehend";
  protected static final String SAMPLE_PRODUCT_PATH = "/market/connector/amazon-comprehend";
  protected static final String SAMPLE_PRODUCT_NAME = "prody Comprehend";
  protected static final Pageable PAGEABLE = PageRequest.of(0, 20,
      Sort.by(SortOption.ALPHABETICALLY.getOption()).descending());
  protected static final String MOCK_PRODUCT_ID = "bpmn-statistic";
  protected static final String MOCK_PRODUCT_ID_WITH_TAG = "bpmn-statistic-10.0.10";
  protected static final String MOCK_ARTIFACT_ID = "bpmn-statistic";
  protected static final String MOCK_PRODUCT_ARTIFACT_ID = "bpmn-statistic-product";
  protected static final String MOCK_RELEASED_VERSION = "10.0.10";
  protected static final String MOCK_SNAPSHOT_VERSION = "10.0.10-SNAPSHOT";
  protected static final String MOCK_BUGFIX_VERSION = "10.0.10.1";
  protected static final String MOCK_SPRINT_RELEASED_VERSION = "10.0.10-m123";
  protected static final String MOCK_TAG_FROM_RELEASED_VERSION = "v10.0.10";
  protected static final String MOCK_TAG_FROM_SNAPSHOT_VERSION = "v10.0.10-SNAPSHOT";
  protected static final String MOCK_GROUP_ID = "com.axonivy.util";
  protected static final String MOCK_PRODUCT_NAME = "bpmn statistic";
  protected static final String MOCK_PRODUCT_JSON_FILE_PATH = "src/test/resources/product.json";
  protected static final String MOCK_PRODUCT_JSON_FILE_PATH_NO_URL = "src/test/resources/productMissingURL.json";
  protected static final String MOCK_PRODUCT_JSON_DIR_PATH = "src/test/resources";
  protected static final String MOCK_PRODUCT_JSON_NODE_FILE_PATH = "src/test/resources/prouct-json-node.json";
  protected static final String MOCK_METADATA_FILE_PATH = "src/test/resources/metadata.xml";
  protected static final String MOCK_SNAPSHOT_METADATA_FILE_PATH = "src/test/resources/snapshotMetadata.xml";
  protected static final String INVALID_FILE_PATH = "test/file/path";
  protected static final String MOCK_SETUP_MD_PATH = "src/test/resources/setup.md";
  protected static final String MOCK_MAVEN_URL = "https://maven.axonivy.com/com/axonivy/util/bpmn-statistic/maven" +
      "-metadata.xml";
  protected static final String MOCK_SNAPSHOT_MAVEN_URL = "https://maven.axonivy.com/com/axonivy/util/bpmn-statistic" +
      "/10.0.10-SNAPSHOT/maven-metadata.xml";
  protected static final String MOCK_DOWNLOAD_URL = "https://maven.axonivy.com/com/axonivy/util/bpmn-statistic/10.0" +
      ".10/bpmn-statistic-10.0.10.zip";
  protected static final String MOCK_SNAPSHOT_DOWNLOAD_URL = "https://maven.axonivy" +
      ".com/com/axonivy/util/bpmn-statistic/10.0.10-SNAPSHOT/bpmn-statistic-10.0.10-SNAPSHOT.zip";
  protected static final String MOCK_ARTIFACT_NAME = "bpmn statistic (zip)";
  protected static final String MOCK_ARTIFACT_DOWNLOAD_FILE = "bpmn-statistic.zip";
  protected static final String LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME = "legacyInstallationCountPath";

  protected Page<Product> createPageProductsMock() {
    var mockProducts = new ArrayList<Product>();
    Map<String, String> name = new HashMap<>();
    Product mockProduct = new Product();
    mockProduct.setId(SAMPLE_PRODUCT_ID);
    name.put(Language.EN.getValue(), SAMPLE_PRODUCT_NAME);
    mockProduct.setNames(name);
    mockProduct.setType("connector");
    mockProduct.setMarketDirectory(SAMPLE_PRODUCT_ID);
    mockProducts.add(mockProduct);

    mockProduct = new Product();
    mockProduct.setId("tel-search-ch-connector");
    name = new HashMap<>();
    name.put(Language.EN.getValue(), "Swiss phone directory");
    mockProduct.setNames(name);
    mockProduct.setType("util");
    mockProduct.setMarketDirectory(SAMPLE_PRODUCT_ID);
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

  protected static String getMockSetupMd() {
    return getContentFromTestResourcePath(MOCK_SETUP_MD_PATH);
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

  protected ProductModuleContent getMockProductModuleContent() {
    ProductModuleContent mockProductModuleContent = new ProductModuleContent();
    mockProductModuleContent.setMavenVersions(new HashSet<>());
    return mockProductModuleContent;
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

  protected String getMockSnapShotMetadataContent() {
    return getContentFromTestResourcePath(MOCK_SNAPSHOT_METADATA_FILE_PATH);
  }

  protected String getMockMetadataContent() {
    return getContentFromTestResourcePath(MOCK_METADATA_FILE_PATH);
  }

  protected Metadata buildMocKMetadata() {
    return Metadata.builder().url(
        MOCK_MAVEN_URL).repoUrl(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL).groupId(MOCK_GROUP_ID).artifactId(
        MOCK_ARTIFACT_ID).type(MavenConstants.DEFAULT_PRODUCT_FOLDER_TYPE).productId(MOCK_PRODUCT_ID).build();
  }

  protected MavenArtifactVersion getMockMavenArtifactVersion() {
    return new MavenArtifactVersion(StringUtils.EMPTY, new HashMap<>(),
        new HashMap<>());
  }

  protected Product getMockProduct() {
    return Product.builder().id(MOCK_PRODUCT_ID).releasedVersions(List.of(MOCK_RELEASED_VERSION)).artifacts(
        List.of(getMockArtifact())).build();
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

  protected MavenArtifactModel getMockMavenArtifactModel() {
    MavenArtifactModel mockMavenArtifactModel = new MavenArtifactModel();
    mockMavenArtifactModel.setName(MOCK_ARTIFACT_NAME);
    mockMavenArtifactModel.setDownloadUrl(MOCK_DOWNLOAD_URL);
    return mockMavenArtifactModel;
  }

  protected MavenArtifactModel getMockMavenArtifactModelWithDownloadUrl() {
    return MavenArtifactModel.builder().name(MOCK_PRODUCT_NAME).artifactId(MOCK_ARTIFACT_ID).downloadUrl(
        MOCK_DOWNLOAD_URL).build();
  }
}
