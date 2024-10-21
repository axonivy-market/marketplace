package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.bo.Artifact;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.constants.ReadmeConstants;
import com.axonivy.market.entity.Image;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.github.service.impl.GHAxonIvyProductRepoServiceImpl;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.service.ImageService;
import com.axonivy.market.util.MavenUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.kohsuke.github.PagedIterable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GHAxonIvyProductRepoServiceImplTest extends BaseSetup {

  public static final String IMAGE_NAME = "image.png";
  public static final String DOCUWARE_CONNECTOR_PRODUCT = "docuware-connector-product";
  private final ObjectMapper objectMapper = new ObjectMapper();

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
  GHContent content = new GHContent();

  @Mock
  ImageService imageService;

  @Mock
  ProductJsonContentRepository productJsonContentRepository;

  @InjectMocks
  @Spy
  private GHAxonIvyProductRepoServiceImpl axonivyProductRepoServiceImpl;

  void setup() throws IOException {
    when(gitHubService.getOrganization(any())).thenReturn(mockGHOrganization);
    when(mockGHOrganization.getRepository(any())).thenReturn(ghRepository);
  }

  private static GHContent createMockImage() {
    GHContent mockImage = mock(GHContent.class);
    when(mockImage.isFile()).thenReturn(true);
    when(mockImage.getName()).thenReturn(IMAGE_NAME);
    return mockImage;
  }

  @AfterEach
  void after() {
    Mockito.reset(mockGHOrganization);
    Mockito.reset(gitHubService);
  }

  @Test
  void testContentFromGHRepoAndTag() throws IOException {
    setup();
    var result = axonivyProductRepoServiceImpl.getContentFromGHRepoAndTag(StringUtils.EMPTY, null, null);
    assertNull(result);
    when(axonivyProductRepoServiceImpl.getOrganization()).thenThrow(IOException.class);
    result = axonivyProductRepoServiceImpl.getContentFromGHRepoAndTag(StringUtils.EMPTY, null, null);
    assertNull(result);
  }

  @Test
  void testExtractMavenArtifactFromJsonNode() throws JsonProcessingException {
    List<Artifact> artifacts = new ArrayList<>();
    // Arrange
    String mockJsonNode = getMockProductJsonNodeContent();

    dataNode = objectMapper.readTree(mockJsonNode);
    boolean isDependency = true;

    MavenUtils.extractMavenArtifactFromJsonNode(dataNode, isDependency, artifacts,
        MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);

    assertEquals(2, artifacts.size());  // Assert that 2 artifacts were added
    assertEquals(MOCK_ARTIFACT_ID, artifacts.get(0).getArtifactId());  // Validate first artifact
    assertEquals(MOCK_GROUP_ID, artifacts.get(1).getGroupId());
  }

  private static void getReadmeInputStream(String readmeContentString, GHContent mockContent) throws IOException {
    InputStream mockReadmeInputStream = mock(InputStream.class);
    when(mockContent.read()).thenReturn(mockReadmeInputStream);
    when(mockReadmeInputStream.readAllBytes()).thenReturn(readmeContentString.getBytes());
  }

  @Test
  void testGetOrganization() throws IOException {
    when(gitHubService.getOrganization(anyString())).thenReturn(mockGHOrganization);
    assertEquals(mockGHOrganization, axonivyProductRepoServiceImpl.getOrganization());
    assertEquals(mockGHOrganization, axonivyProductRepoServiceImpl.getOrganization());
  }

  //TODO
//  @Test
//  void testGetReadmeAndProductContentsFromTag() throws IOException {
//    String readmeContentWithImage = """
//        #Product-name
//        Test README
//        ## Demo
//        Demo content
//        ## Setup
//        Setup content (image.png)
//        """;
//    testGetReadmeAndProductContentsFromTagWithReadmeText(readmeContentWithImage);
//    String readmeContentWithoutHashProductName = """
//        Test README
//        ## Demo
//        Demo content
//        ## Setup
//        Setup content (image.png)
//        """;
//    testGetReadmeAndProductContentsFromTagWithReadmeText(readmeContentWithoutHashProductName);
//  }

  @Test
  void testCreateArtifactFromJsonNode() {
    String repoUrl = MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL;
    boolean isDependency = true;
    String groupId = MOCK_GROUP_ID;
    String artifactId = MOCK_ARTIFACT_ID;
    String type = ProductJsonConstants.DEFAULT_PRODUCT_TYPE;

    JsonNode groupIdNode = mock(JsonNode.class);
    JsonNode artifactIdNode = mock(JsonNode.class);
    JsonNode typeNode = mock(JsonNode.class);
    when(groupIdNode.asText()).thenReturn(groupId);
    when(artifactIdNode.asText()).thenReturn(artifactId);
    when(typeNode.asText()).thenReturn(type);
    when(dataNode.path(ProductJsonConstants.GROUP_ID)).thenReturn(groupIdNode);
    when(dataNode.path(ProductJsonConstants.ARTIFACT_ID)).thenReturn(artifactIdNode);
    when(dataNode.path(ProductJsonConstants.TYPE)).thenReturn(typeNode);

    Artifact artifact = MavenUtils.createArtifactFromJsonNode(dataNode, repoUrl, isDependency);

    assertEquals(repoUrl, artifact.getRepoUrl());
    assertTrue(artifact.getIsDependency());
    assertEquals(groupId, artifact.getGroupId());
    assertEquals(artifactId, artifact.getArtifactId());
    assertEquals(type, artifact.getType());
    assertTrue(artifact.getIsProductArtifact());
  }

  public static Image mockImage() {
    Image image = new Image();
    image.setId("66e2b14868f2f95b2f95549a");
    image.setSha("914d9b6956db7a1404622f14265e435f36db81fa");
    image.setProductId("amazon-comprehend");
    image.setImageUrl(
        "https://raw.githubusercontent.com/amazon-comprehend-connector-product/images/comprehend-demo-sentiment.png");
    return image;
  }

  @Test
  void testGetReadmeAndProductContentFromTag_ImageFromRootFolder() {
    String readmeContentWithImageFolder = """
        #Product-name
        Test README
        ## Demo
        Demo content
        ## Setup
        Setup content (./image.png)""";

    GHContent mockImageFile = mock(GHContent.class);
    when(mockImageFile.getName()).thenReturn(IMAGE_NAME);
    when(imageService.mappingImageFromGHContent(any(), any(), anyBoolean())).thenReturn(mockImage());

    String updatedReadme = axonivyProductRepoServiceImpl.updateImagesWithDownloadUrl(BaseSetup.MOCK_PRODUCT_ID,
        List.of(mockImageFile), readmeContentWithImageFolder);

    assertEquals("""
            #Product-name
            Test README
            ## Demo
            Demo content
            ## Setup
            Setup content (imageId-66e2b14868f2f95b2f95549a)""",
        updatedReadme);
  }

  @Test
  void testGetReadmeAndProductContentFromTag_ImageFromChildFolder() throws IOException {
    String readmeContentWithImageFolder = """
        #Product-name
        Test README
        ## Demo
        Demo content
        ## Setup
        Setup content (.doc/img/image.png)""";

    GHContent mockImageFile = mock(GHContent.class);
    when(mockImageFile.isDirectory()).thenReturn(true);

    GHContent mockImageFile2 = mock(GHContent.class);
    when(mockImageFile2.isDirectory()).thenReturn(true);

    GHContent mockImageFile3 = mock(GHContent.class);
    when(mockImageFile3.getName()).thenReturn(IMAGE_NAME);

    PagedIterable<GHContent> pagedIterable = mock(String.valueOf(GHContent.class));
    when(mockImageFile.listDirectoryContent()).thenReturn(pagedIterable);
    when(pagedIterable.toList()).thenReturn(List.of(mockImageFile2));

    PagedIterable<GHContent> pagedIterable2 = mock(String.valueOf(GHContent.class));
    when(mockImageFile2.listDirectoryContent()).thenReturn(pagedIterable2);
    when(pagedIterable2.toList()).thenReturn(List.of(mockImageFile3));

    when(imageService.mappingImageFromGHContent(any(), any(), anyBoolean())).thenReturn(mockImage());

    String updatedReadme = axonivyProductRepoServiceImpl.updateImagesWithDownloadUrl(BaseSetup.MOCK_PRODUCT_ID,
        List.of(mockImageFile), readmeContentWithImageFolder);

    assertEquals("""
            #Product-name
            Test README
            ## Demo
            Demo content
            ## Setup
            Setup content (imageId-66e2b14868f2f95b2f95549a)""",
        updatedReadme);
  }

  //TODO
//  private void testGetReadmeAndProductContentsFromTagWithReadmeText(String readmeContentWithImage) throws
//  IOException {
//    //Mock readme content
//    GHContent mockContent = mock(GHContent.class);
//    when(mockContent.isDirectory()).thenReturn(true);
//    when(mockContent.isFile()).thenReturn(true);
//    when(mockContent.getName()).thenReturn(DOCUWARE_CONNECTOR_PRODUCT, ReadmeConstants.README_FILE);
//
//    PagedIterable<GHContent> pagedIterable = mock(String.valueOf(GHContent.class));
//    when(mockContent.listDirectoryContent()).thenReturn(pagedIterable);
//    when(pagedIterable.toList()).thenReturn(List.of());
//
//    getReadmeInputStream(readmeContentWithImage, mockContent);
//
//    GHContent mockContent2 = createMockImage();
//
//    when(ghRepository.getDirectoryContent(CommonConstants.SLASH, MOCK_TAG_FROM_RELEASED_VERSION)).thenReturn(
//        List.of(mockContent, mockContent2));
//    when(ghRepository.getDirectoryContent(DOCUWARE_CONNECTOR_PRODUCT, MOCK_TAG_FROM_RELEASED_VERSION)).thenReturn(
//        List.of(mockContent, mockContent2));
//    when(imageService.mappingImageFromGHContent(any(), any(), anyBoolean())).thenReturn(mockImage());
//    var result = axonivyProductRepoServiceImpl.extractReadMeFileFromContents(getMockProducts().get(0),
//        ghRepository,
//        MOCK_TAG_FROM_RELEASED_VERSION);
//
//    assertEquals(MOCK_TAG_FROM_RELEASED_VERSION, result.getTag());
//    assertEquals("Test README", result.getDescription().get(Language.EN.getValue()));
//    assertEquals("Demo content", result.getDemo().get(Language.EN.getValue()));
//    assertEquals("Setup content (imageId-66e2b14868f2f95b2f95549a)", result.getSetup().get(Language.EN.getValue()));
//  }

  //TODO
//  @Test
//  void testGetReadmeAndProductContentsFromTag_WithNoFullyThreeParts() throws IOException {
//    String readmeContentString = "#Product-name\n Test README\n## Setup\nSetup content";
//    GHContent mockContent = createMockProductFolder();
//    getReadmeInputStream(readmeContentString, mockContent);
//
//    var result = axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag(getMockProducts().get(0),
//        ghRepository,
//        MOCK_TAG_FROM_RELEASED_VERSION);
//
//    assertNull(result.getArtifactId());
//    assertEquals("Setup content", result.getSetup().get(Language.EN.getValue()));
//  }

  @Test
  void testConvertProductJsonToMavenProductInfo() throws IOException {
    assertTrue(CollectionUtils.isEmpty(GitHubUtils.convertProductJsonToMavenProductInfo(null)));
    assertTrue(CollectionUtils.isEmpty(GitHubUtils.convertProductJsonToMavenProductInfo(content)));

    InputStream inputStream = getMockInputStream();
    when(GitHubUtils.extractedContentStream(content)).thenReturn(inputStream);
    assertEquals(2, GitHubUtils.convertProductJsonToMavenProductInfo(content).size());
    inputStream = getMockInputStreamWithOutProjectAndDependency();
    when(GitHubUtils.extractedContentStream(content)).thenReturn(inputStream);
    assertTrue(CollectionUtils.isEmpty(GitHubUtils.convertProductJsonToMavenProductInfo(content)));
  }

  //TODO
//  @Test
//  void testGetReadmeAndProductContentsFromTag_SwitchPartsPosition() throws IOException {
//    String readmeContentString = "#Product-name\n Test README\n## Setup\nSetup content\n## Demo\nDemo content";
//    GHContent mockContent = createMockProductFolder();
//    getReadmeInputStream(readmeContentString, mockContent);
//
//    var result = axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag(getMockProducts().get(0),
//        ghRepository,
//        MOCK_TAG_FROM_RELEASED_VERSION);
//    assertEquals("Demo content", result.getDemo().get(Language.EN.getValue()));
//    assertEquals("Setup content", result.getSetup().get(Language.EN.getValue()));
//  }

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

  private GHContent createMockProductFolder() throws IOException {
    GHContent mockContent = mock(GHContent.class);
    when(mockContent.isDirectory()).thenReturn(true);
    when(mockContent.isFile()).thenReturn(true);
    when(mockContent.getName()).thenReturn(DOCUWARE_CONNECTOR_PRODUCT, ReadmeConstants.README_FILE);

    when(ghRepository.getDirectoryContent(CommonConstants.SLASH, MOCK_TAG_FROM_RELEASED_VERSION)).thenReturn(
        List.of(mockContent));
    when(ghRepository.getDirectoryContent(DOCUWARE_CONNECTOR_PRODUCT, MOCK_TAG_FROM_RELEASED_VERSION)).thenReturn(
        List.of(mockContent));

    return mockContent;
  }

  @Test
  void testExtractedContentStream() {
    assertNull(GitHubUtils.extractedContentStream(null));
    assertNull(GitHubUtils.extractedContentStream(content));
  }
}
