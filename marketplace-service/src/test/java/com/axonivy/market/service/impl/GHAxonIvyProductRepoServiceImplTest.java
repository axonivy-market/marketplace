package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.Artifact;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.github.service.impl.GHAxonIvyProductRepoServiceImpl;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.model.ReadmeContentsModel;
import com.axonivy.market.service.ImageService;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.ProductContentUtils;
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
import org.kohsuke.github.PagedIterable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GHAxonIvyProductRepoServiceImplTest extends BaseSetup {

  public static final String IMAGE_NAME = "image.png";
  private final ObjectMapper objectMapper = new ObjectMapper();

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

  @InjectMocks
  @Spy
  private GHAxonIvyProductRepoServiceImpl axonivyProductRepoServiceImpl;

  void setup() throws IOException {
    when(gitHubService.getOrganization(any())).thenReturn(mockGHOrganization);
    when(mockGHOrganization.getRepository(any())).thenReturn(ghRepository);
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
    assertNull(result, "Expected result to be null when calling getContentFromGHRepoAndTag with empty repo and null tag");

    when(axonivyProductRepoServiceImpl.getOrganization()).thenThrow(IOException.class);
    result = axonivyProductRepoServiceImpl.getContentFromGHRepoAndTag(StringUtils.EMPTY, null, null);
    assertNull(result, "Expected result to be null when getOrganization throws IOException");
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

    assertEquals(2, artifacts.size(), "Expected 2 artifacts to be added to the list"); // Assert that 2 artifacts were added
    assertEquals(MOCK_ARTIFACT_ID, artifacts.get(0).getArtifactId(), "First artifact's artifactId does not match"); // Validate first artifact
    assertEquals(MOCK_GROUP_ID, artifacts.get(1).getGroupId(), "Second artifact's groupId does not match");
  }

  @Test
  void testGetOrganization() throws IOException {
    when(gitHubService.getOrganization(anyString())).thenReturn(mockGHOrganization);
    assertEquals(mockGHOrganization, axonivyProductRepoServiceImpl.getOrganization(),
        "Expected getOrganization() to return the mocked GHOrganization on first call");

    assertEquals(mockGHOrganization, axonivyProductRepoServiceImpl.getOrganization(),
        "Expected getOrganization() to return the mocked GHOrganization on second call");
  }

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

    assertEquals(repoUrl, artifact.getRepoUrl(), "Artifact repoUrl does not match expected value");
    assertTrue(artifact.getIsDependency(), "Artifact should be marked as a dependency");
    assertEquals(groupId, artifact.getGroupId(), "Artifact groupId does not match expected value");
    assertEquals(artifactId, artifact.getArtifactId(), "Artifact artifactId does not match expected value");
    assertEquals(type, artifact.getType(), "Artifact type does not match expected value");
    assertTrue(artifact.getIsProductArtifact(), "Artifact should be marked as a product artifact");
  }

  @Test
  void testExtractReadMeFileFromContents_ImageFromRootFolder() {
    String readmeContentWithImageFolder = """
        #Product-name
        Test README
        ## Demo
        Demo content
        ## Setup
        Setup content (./image.png)""";

    GHContent mockImageFile = mock(GHContent.class);
    when(mockImageFile.getName()).thenReturn(IMAGE_NAME);
    when(imageService.mappingImageFromGHContent(any(), any())).thenReturn(getMockImage());

    String updatedReadme = axonivyProductRepoServiceImpl.updateImagesWithDownloadUrl(BaseSetup.MOCK_PRODUCT_ID,
        List.of(mockImageFile), readmeContentWithImageFolder);

    assertEquals(
        """
        #Product-name
        Test README
        ## Demo
        Demo content
        ## Setup
        Setup content (imageId-66e2b14868f2f95b2f95549a)""",
        updatedReadme,
        "Expected README content to have the image URL updated correctly"
    );
  }

  @Test
  void testExtractReadMeFileFromContents_ImageFromChildFolder() throws IOException {
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

    when(imageService.mappingImageFromGHContent(any(), any())).thenReturn(getMockImage());

    String updatedReadme = axonivyProductRepoServiceImpl.updateImagesWithDownloadUrl(BaseSetup.MOCK_PRODUCT_ID,
        List.of(mockImageFile), readmeContentWithImageFolder);

    assertEquals(
        """
        #Product-name
        Test README
        ## Demo
        Demo content
        ## Setup
        Setup content (imageId-66e2b14868f2f95b2f95549a)""",
        updatedReadme,
        "Expected README content to correctly update image URLs from child folders"
    );
  }

  @Test
  void testExtractReadMeFileFromContents() throws IOException {
    try (MockedStatic<ProductContentUtils> mockedProductContentUtils = Mockito.mockStatic(ProductContentUtils.class)) {
      String mockReadmeContent = "Test README content";
      GHContent mockReadmeFile = mock(GHContent.class);
      when(mockReadmeFile.isFile()).thenReturn(true);
      when(mockReadmeFile.getName()).thenReturn("README.md");
      when(mockReadmeFile.read()).thenReturn(new ByteArrayInputStream(mockReadmeContent.getBytes()));

      GHContent mockImageFile = mock(GHContent.class);
      when(mockImageFile.isFile()).thenReturn(true);
      when(mockImageFile.getName()).thenReturn("image.png");

      List<GHContent> contents = List.of(mockReadmeFile, mockImageFile);

      ReadmeContentsModel readmeContentsModel = new ReadmeContentsModel();
      readmeContentsModel.setDescription(mockReadmeContent);

      when(ProductContentUtils.hasImageDirectives(anyString())).thenReturn(true);
      when(ProductContentUtils.getExtractedPartsOfReadme(nullable(String.class))).thenReturn(readmeContentsModel);

      when(imageService.mappingImageFromGHContent(anyString(), any())).thenReturn(getMockImage());
      ProductModuleContent productModuleContent = new ProductModuleContent();

      axonivyProductRepoServiceImpl.extractReadMeFileFromContents(getMockProducts().get(0), contents,
          productModuleContent);

      verify(mockReadmeFile, times(1)).read();
      verify(imageService, times(1)).mappingImageFromGHContent(anyString(), eq(mockImageFile));
    }
  }

  @Test
  void testConvertProductJsonToMavenProductInfo() throws IOException {
    assertTrue(CollectionUtils.isEmpty(GitHubUtils.convertProductJsonToMavenProductInfo(null)),
        "Expected result to be empty when input JSON content is null");

    assertTrue(CollectionUtils.isEmpty(GitHubUtils.convertProductJsonToMavenProductInfo(content)),
        "Expected result to be empty when input JSON content is empty");

    InputStream inputStream = getMockInputStream();
    when(GitHubUtils.extractedContentStream(content)).thenReturn(inputStream);
    assertEquals(2, GitHubUtils.convertProductJsonToMavenProductInfo(content).size(),
        "Expected 2 Maven product info objects when mock input stream contains products");

    inputStream = getMockInputStreamWithOutProjectAndDependency();
    when(GitHubUtils.extractedContentStream(content)).thenReturn(inputStream);
    assertTrue(CollectionUtils.isEmpty(GitHubUtils.convertProductJsonToMavenProductInfo(content)),
        "Expected result to be empty when input stream has no project or dependency");
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

  @Test
  void testExtractedContentStream() {
    assertNull(GitHubUtils.extractedContentStream(null),
        "Expected extractedContentStream to return null when input content is null");

    assertNull(GitHubUtils.extractedContentStream(content),
        "Expected extractedContentStream to return null when input content is empty or invalid");
  }
}
