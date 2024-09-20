package com.axonivy.market.service.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.constants.ReadmeConstants;
import com.axonivy.market.entity.Image;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.enums.Language;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.github.service.impl.GHAxonIvyProductRepoServiceImpl;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.service.ImageService;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.kohsuke.github.PagedIterable;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GHAxonIvyProductRepoServiceImplTest {

  public static final String RELEASE_TAG = "v10.0.0";
  public static final String IMAGE_NAME = "image.png";
  public static final String DOCUWARE_CONNECTOR_PRODUCT = "docuware-connector-product";
  public static final String IMAGE_DOWNLOAD_URL = "https://raw.githubusercontent.com/image.png";
  private static final String DUMMY_TAG = "v1.0.0";
  @Mock
  PagedIterable<GHTag> listTags;

  @Mock
  GHRepository ghRepository;

  @Mock
  GitHubService gitHubService;

  GHOrganization mockGHOrganization = mock(GHOrganization.class);

  @Mock
  JsonNode dataNode;

  @Mock
  JsonNode childNode;

  @Mock
  GHContent content = new GHContent();

  @Mock
  ImageService imageService;

  @Mock
  ProductJsonContentRepository productJsonContentRepository;

  @InjectMocks
  @Spy
  private GHAxonIvyProductRepoServiceImpl axonivyProductRepoServiceImpl;

  public static Image mockImage() {
    Image image = new Image();
    image.setId("66e2b14868f2f95b2f95549a");
    image.setSha("914d9b6956db7a1404622f14265e435f36db81fa");
    image.setProductId("amazon-comprehend");
    image.setImageUrl(
        "https://raw.githubusercontent.com/amazon-comprehend-connector-product/images/comprehend-demo-sentiment.png");
    return image;
  }

  private static void getReadmeInputStream(String readmeContentString, GHContent mockContent) throws IOException {
    InputStream mockReadmeInputStream = mock(InputStream.class);
    when(mockContent.read()).thenReturn(mockReadmeInputStream);
    when(mockReadmeInputStream.readAllBytes()).thenReturn(readmeContentString.getBytes());
  }

  private static InputStream getMockInputStream() {
    String jsonContent = getMockProductJsonContent();
    return new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));
  }

  private static String getMockProductJsonContent() {
    return """
        {
           "$schema": "https://json-schema.axonivy.com/market/10.0.0/product.json",
           "installers": [
             {
               "id": "maven-import",
               "data": {
                 "projects": [
                   {
                     "groupId": "com.axonivy.utils.bpmnstatistic",
                     "artifactId": "bpmn-statistic-demo",
                     "version": "${version}",
                     "type": "iar"
                   }
                 ],
                 "repositories": [
                   {
                     "id": "maven.axonivy.com",
                     "url": "https://maven.axonivy.com",
                     "snapshots": {
                       "enabled": "true"
                     }
                   }
                 ]
               }
             },
             {
               "id": "maven-dependency",
               "data": {
                 "dependencies": [
                   {
                     "groupId": "com.axonivy.utils.bpmnstatistic",
                     "artifactId": "bpmn-statistic",
                     "version": "${version}",
                     "type": "iar"
                   }
                 ],
                 "repositories": [
                   {
                     "id": "maven.axonivy.com",
                     "url": "https://maven.axonivy.com",
                     "snapshots": {
                       "enabled": "true"
                     }
                   }
                 ]
               }
             }
           ]
         }
        """;
  }

  private static InputStream getMockInputStreamWithOutProjectAndDependency() {
    String jsonContent = """
        {
          "installers": [
            {
              "data": {
                "repositories": [
                  {
                    "url": "http://example.com/repo"
                  }
                ]
              }
            }
          ]
        }
        """;
    return new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));
  }

  private static GHContent createMockProductJson() {
    GHContent mockProductJson = mock(GHContent.class);
    when(mockProductJson.isFile()).thenReturn(true);
    when(mockProductJson.getName()).thenReturn(ProductJsonConstants.PRODUCT_JSON_FILE, IMAGE_NAME);
    return mockProductJson;
  }

  void setup() throws IOException {
    when(gitHubService.getOrganization(any())).thenReturn(mockGHOrganization);
    when(mockGHOrganization.getRepository(any())).thenReturn(ghRepository);
  }

  @AfterEach
  void after() {
    reset(mockGHOrganization);
    reset(gitHubService);
  }

  @Test
  void testAllTagsFromRepoName() throws IOException {
    setup();
    var mockTag = mock(GHTag.class);
    when(mockTag.getName()).thenReturn(DUMMY_TAG);
    when(listTags.toList()).thenReturn(List.of(mockTag));
    when(ghRepository.listTags()).thenReturn(listTags);
    var result = axonivyProductRepoServiceImpl.getAllTagsFromRepoName("");
    assertEquals(1, result.size());
    assertEquals(DUMMY_TAG, result.get(0).getName());
  }

  @Test
  void testContentFromGHRepoAndTag() throws IOException {
    setup();
    var result = axonivyProductRepoServiceImpl.getContentFromGHRepoAndTag("", null, null);
    assertNull(result);
    when(axonivyProductRepoServiceImpl.getOrganization()).thenThrow(IOException.class);
    result = axonivyProductRepoServiceImpl.getContentFromGHRepoAndTag("", null, null);
    assertNull(result);
  }

  @Test
  void testExtractMavenArtifactFromJsonNode() {
    List<MavenArtifact> artifacts = new ArrayList<>();
    boolean isDependency = true;
    String nodeName = ProductJsonConstants.DEPENDENCIES;

    createListNodeForDataNoteByName(nodeName);
    MavenArtifact mockArtifact = Mockito.mock(MavenArtifact.class);
    Mockito.doReturn(mockArtifact).when(axonivyProductRepoServiceImpl)
        .createArtifactFromJsonNode(childNode, null, isDependency);

    axonivyProductRepoServiceImpl.extractMavenArtifactFromJsonNode(dataNode, isDependency, artifacts);

    assertEquals(1, artifacts.size());
    assertSame(mockArtifact, artifacts.get(0));

    isDependency = false;
    nodeName = ProductJsonConstants.PROJECTS;
    createListNodeForDataNoteByName(nodeName);

    Mockito.doReturn(mockArtifact).when(axonivyProductRepoServiceImpl)
        .createArtifactFromJsonNode(childNode, null, isDependency);

    axonivyProductRepoServiceImpl.extractMavenArtifactFromJsonNode(dataNode, isDependency, artifacts);

    assertEquals(2, artifacts.size());
    assertSame(mockArtifact, artifacts.get(1));
  }

  private void createListNodeForDataNoteByName(String nodeName) {
    JsonNode sectionNode = Mockito.mock(JsonNode.class);
    Iterator<JsonNode> iterator = Mockito.mock(String.valueOf(Iterator.class));
    Mockito.when(dataNode.path(nodeName)).thenReturn(sectionNode);
    Mockito.when(sectionNode.iterator()).thenReturn(iterator);
    Mockito.when(iterator.hasNext()).thenReturn(true, false);
    Mockito.when(iterator.next()).thenReturn(childNode);
  }

  @Test
  void testCreateArtifactFromJsonNode() {
    String repoUrl = "http://example.com/repo";
    boolean isDependency = true;
    String groupId = "com.example";
    String artifactId = "example-artifact";
    String type = "jar";

    JsonNode groupIdNode = Mockito.mock(JsonNode.class);
    JsonNode artifactIdNode = Mockito.mock(JsonNode.class);
    JsonNode typeNode = Mockito.mock(JsonNode.class);
    Mockito.when(groupIdNode.asText()).thenReturn(groupId);
    Mockito.when(artifactIdNode.asText()).thenReturn(artifactId);
    Mockito.when(typeNode.asText()).thenReturn(type);
    Mockito.when(dataNode.path(ProductJsonConstants.GROUP_ID)).thenReturn(groupIdNode);
    Mockito.when(dataNode.path(ProductJsonConstants.ARTIFACT_ID)).thenReturn(artifactIdNode);
    Mockito.when(dataNode.path(ProductJsonConstants.TYPE)).thenReturn(typeNode);

    MavenArtifact artifact = axonivyProductRepoServiceImpl.createArtifactFromJsonNode(dataNode, repoUrl, isDependency);

    assertEquals(repoUrl, artifact.getRepoUrl());
    assertTrue(artifact.getIsDependency());
    assertEquals(groupId, artifact.getGroupId());
    assertEquals(artifactId, artifact.getArtifactId());
    assertEquals(type, artifact.getType());
    assertTrue(artifact.getIsProductArtifact());
  }

  @Test
  void testConvertProductJsonToMavenProductInfo() throws IOException {
    assertEquals(0, axonivyProductRepoServiceImpl.convertProductJsonToMavenProductInfo(null).size());
    assertEquals(0, axonivyProductRepoServiceImpl.convertProductJsonToMavenProductInfo(content).size());

    InputStream inputStream = getMockInputStream();
    Mockito.when(axonivyProductRepoServiceImpl.extractedContentStream(content)).thenReturn(inputStream);
    assertEquals(2, axonivyProductRepoServiceImpl.convertProductJsonToMavenProductInfo(content).size());
    inputStream = getMockInputStreamWithOutProjectAndDependency();
    Mockito.when(axonivyProductRepoServiceImpl.extractedContentStream(content)).thenReturn(inputStream);
    assertEquals(0, axonivyProductRepoServiceImpl.convertProductJsonToMavenProductInfo(content).size());
  }

  @Test
  void testExtractedContentStream() {
    assertNull(axonivyProductRepoServiceImpl.extractedContentStream(null));
    assertNull(axonivyProductRepoServiceImpl.extractedContentStream(content));
  }

  @Test
  void testGetOrganization() throws IOException {
    Mockito.when(gitHubService.getOrganization(Mockito.anyString())).thenReturn(mockGHOrganization);
    assertEquals(mockGHOrganization, axonivyProductRepoServiceImpl.getOrganization());
    assertEquals(mockGHOrganization, axonivyProductRepoServiceImpl.getOrganization());
  }

  @Test
  void testGetReadmeAndProductContentsFromTag() throws IOException {
    String readmeContentWithImage = "#Product-name\n Test README\n## Demo\nDemo content\n## Setup\nSetup content " +
        "(image.png)";
    testGetReadmeAndProductContentsFromTagWithReadmeText(readmeContentWithImage);
    String readmeContentWithoutHashProductName = "Test README\n## Demo\nDemo content\n## Setup\nSetup content (image" +
        ".png)";
    testGetReadmeAndProductContentsFromTagWithReadmeText(readmeContentWithoutHashProductName);
  }

  private void testGetReadmeAndProductContentsFromTagWithReadmeText(String readmeContentWithImage) throws IOException {
    GHContent mockContent = createMockProductFolderWithProductJson();

    getReadmeInputStream(readmeContentWithImage, mockContent);
    InputStream inputStream = getMockInputStream();
    Mockito.when(axonivyProductRepoServiceImpl.extractedContentStream(any())).thenReturn(inputStream);
    Mockito.when(imageService.mappingImageFromGHContent(any(), any(), anyBoolean())).thenReturn(mockImage());
    var result = axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag(createMockProduct(), ghRepository,
        RELEASE_TAG);

    assertEquals(RELEASE_TAG, result.getTag());
    assertTrue(result.getIsDependency());
    assertEquals("com.axonivy.utils.bpmnstatistic", result.getGroupId());
    assertEquals("bpmn-statistic", result.getArtifactId());
    assertEquals("iar", result.getType());
    assertEquals("Test README", result.getDescription().get(Language.EN.getValue()));
    assertEquals("Demo content", result.getDemo().get(Language.EN.getValue()));
    assertEquals("Setup content (imageId-66e2b14868f2f95b2f95549a)", result.getSetup().get(Language.EN.getValue()));
  }

  @Test
  void testGetReadmeAndProductContentFromTag_ImageFromFolder() throws IOException {
    String readmeContentWithImageFolder = "#Product-name\n Test README\n## Demo\nDemo content\n## Setup\nSetup " +
        "content (./images/image.png)";

    GHContent mockImageFile = mock(GHContent.class);
    when(mockImageFile.getName()).thenReturn(ReadmeConstants.IMAGES, IMAGE_NAME);
    when(mockImageFile.isDirectory()).thenReturn(true);
    Mockito.when(imageService.mappingImageFromGHContent(any(), any(), anyBoolean())).thenReturn(mockImage());
    PagedIterable<GHContent> pagedIterable = Mockito.mock(String.valueOf(GHContent.class));
    when(mockImageFile.listDirectoryContent()).thenReturn(pagedIterable);
    when(pagedIterable.toList()).thenReturn(List.of(mockImageFile));

    String updatedReadme = axonivyProductRepoServiceImpl.updateImagesWithDownloadUrl(createMockProduct(),
        List.of(mockImageFile), readmeContentWithImageFolder);

    assertEquals(
        "#Product-name\n Test README\n## Demo\nDemo content\n## Setup\nSetup content " +
            "(imageId-66e2b14868f2f95b2f95549a)",
        updatedReadme);
  }

  @Test
  void testGetReadmeAndProductContentsFromTag_WithNoFullyThreeParts() throws IOException {
    String readmeContentString = "#Product-name\n Test README\n## Setup\nSetup content";

    GHContent mockContent = createMockProductFolder();

    getReadmeInputStream(readmeContentString, mockContent);

    var result = axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag(createMockProduct(), ghRepository,
        RELEASE_TAG);

    assertNull(result.getArtifactId());
    assertEquals("Setup content", result.getSetup().get(Language.EN.getValue()));
  }

  @Test
  void testGetReadmeAndProductContentsFromTag_SwitchPartsPosition() throws IOException {
    String readmeContentString = "#Product-name\n Test README\n## Setup\nSetup content\n## Demo\nDemo content";

    GHContent mockContent = createMockProductFolder();

    getReadmeInputStream(readmeContentString, mockContent);

    var result = axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag(createMockProduct(), ghRepository,
        RELEASE_TAG);
    assertEquals("Demo content", result.getDemo().get(Language.EN.getValue()));
    assertEquals("Setup content", result.getSetup().get(Language.EN.getValue()));
  }

  private Product createMockProduct() {
    Map<String, String> names = Map.of("en", "docuware-connector-name");
    Product product = new Product();
    product.setId("docuware-connector");
    product.setNames(names);
    product.setLanguage("en");
    return product;
  }

  private GHContent createMockProductFolder() throws IOException {
    GHContent mockContent = mock(GHContent.class);
    when(mockContent.isDirectory()).thenReturn(true);
    when(mockContent.isFile()).thenReturn(true);
    when(mockContent.getName()).thenReturn(DOCUWARE_CONNECTOR_PRODUCT, ReadmeConstants.README_FILE);

    when(ghRepository.getDirectoryContent(CommonConstants.SLASH, RELEASE_TAG)).thenReturn(List.of(mockContent));
    when(ghRepository.getDirectoryContent(DOCUWARE_CONNECTOR_PRODUCT, RELEASE_TAG)).thenReturn(List.of(mockContent));

    return mockContent;
  }

  private GHContent createMockProductFolderWithProductJson() throws IOException {
    GHContent mockContent = mock(GHContent.class);
    when(mockContent.isDirectory()).thenReturn(true);
    when(mockContent.isFile()).thenReturn(true);
    when(mockContent.getName()).thenReturn(DOCUWARE_CONNECTOR_PRODUCT, ReadmeConstants.README_FILE);

    GHContent mockContent2 = createMockProductJson();

    when(ghRepository.getDirectoryContent(CommonConstants.SLASH, RELEASE_TAG)).thenReturn(
        List.of(mockContent, mockContent2));
    when(ghRepository.getDirectoryContent(DOCUWARE_CONNECTOR_PRODUCT, RELEASE_TAG)).thenReturn(
        List.of(mockContent, mockContent2));

    return mockContent;
  }

  @Test
  void test_insertProductJsonContent() throws IOException {
    ArgumentCaptor<ProductJsonContent> argumentCaptor = ArgumentCaptor.forClass(ProductJsonContent.class);
    String readmeContentWithImage = "#Product-name\n Test README\n## Demo\nDemo content\n## Setup\nSetup content " +
        "(image.png)";
    GHContent mockContent = createMockProductFolderWithProductJson();
    getReadmeInputStream(readmeContentWithImage, mockContent);
    InputStream inputStream = getMockInputStream();
    Mockito.when(axonivyProductRepoServiceImpl.extractedContentStream(any())).thenReturn(inputStream);
    Mockito.when(axonivyProductRepoServiceImpl.extractProductJsonContent(any(), anyString())).thenReturn(
        getMockProductJsonContent());

    ProductJsonContent expectedProductJsonContent = new ProductJsonContent();
    expectedProductJsonContent.setProductId("docuware-connector");
    expectedProductJsonContent.setName("docuware-connector-name");
    expectedProductJsonContent.setVersion("10.0.0");
    expectedProductJsonContent.setContent(getMockProductJsonContent());

    axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag(createMockProduct(), ghRepository,
        RELEASE_TAG);

    verify(productJsonContentRepository).save(argumentCaptor.capture());
    assertEquals("docuware-connector-name", argumentCaptor.getValue().getName());
    assertEquals("10.0.0", argumentCaptor.getValue().getVersion());
    assertEquals("docuware-connector", argumentCaptor.getValue().getProductId());
    assertEquals(getMockProductJsonContent().replace("${version}", "10.0.0"), argumentCaptor.getValue().getContent());
  }
}
