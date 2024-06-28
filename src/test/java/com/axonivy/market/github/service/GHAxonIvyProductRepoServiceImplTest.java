package com.axonivy.market.github.service;

import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.github.service.impl.GHAxonIvyProductRepoServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.*;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GHAxonIvyProductRepoServiceImplTest {

  private static final String DUMMY_TAG = "v1.0.0";

  @Mock
  PagedIterable<GHTag> listTags;

  @Mock
  GHRepository ghRepository;

  @Mock
  GitHubService githubService;

  @Mock
  GHOrganization organization;

  @Mock
  JsonNode dataNode;

  @Mock
  JsonNode childNode;

  @Mock
  GHContent content = new GHContent();

  @InjectMocks
  @Spy
  private GHAxonIvyProductRepoServiceImpl axonivyProductRepoServiceImpl;

  void setup() throws IOException {
    var mockGHOrganization = mock(GHOrganization.class);
    when(githubService.getOrganization(any())).thenReturn(mockGHOrganization);
    when(mockGHOrganization.getRepository(any())).thenReturn(ghRepository);
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
    Mockito.doReturn(mockArtifact).when(axonivyProductRepoServiceImpl).createArtifactFromJsonNode(childNode, null, isDependency);

    axonivyProductRepoServiceImpl.extractMavenArtifactFromJsonNode(dataNode, isDependency, artifacts);

    assertEquals(1, artifacts.size());
    assertSame(mockArtifact, artifacts.get(0));

    isDependency = false;
    nodeName = ProductJsonConstants.PROJECTS;
    createListNodeForDataNoteByName(nodeName);

    Mockito.doReturn(mockArtifact).when(axonivyProductRepoServiceImpl).createArtifactFromJsonNode(childNode, null, isDependency);

    axonivyProductRepoServiceImpl.extractMavenArtifactFromJsonNode(dataNode, isDependency, artifacts);

    assertEquals(2, artifacts.size());
    assertSame(mockArtifact, artifacts.get(1));
  }

  private void createListNodeForDataNoteByName(String nodeName) {
    JsonNode sectionNode = Mockito.mock(JsonNode.class);
    Iterator<JsonNode> iterator = Mockito.mock(Iterator.class);
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

  private static InputStream getMockInputStream() {
    String jsonContent = """
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
    return new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));
  }

  private static InputStream getMockInputStreamWithOutProjectAndDependency() {
    String jsonContent = "{\n" +
            "  \"installers\": [\n" +
            "    {\n" +
            "      \"data\": {\n" +
            "        \"repositories\": [\n" +
            "          {\n" +
            "            \"url\": \"http://example.com/repo\"\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    return new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void testExtractedContentStream() {
    assertNull(axonivyProductRepoServiceImpl.extractedContentStream(null));
    assertNull(axonivyProductRepoServiceImpl.extractedContentStream(content));
  }

  @Test
  void testGetOrganization() throws IOException {
    Mockito.when(githubService.getOrganization(Mockito.anyString())).thenReturn(organization);
    assertEquals(organization, axonivyProductRepoServiceImpl.getOrganization());
    assertEquals(organization, axonivyProductRepoServiceImpl.getOrganization());
  }
}
