package com.axonivy.market.github.service;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.github.service.impl.GHAxonIvyProductRepoServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
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


@ExtendWith(MockitoExtension.class)
public class GHAxonIvyProductRepoServiceImplTest {
    @InjectMocks
    @Spy
    private GHAxonIvyProductRepoServiceImpl ghAxonIvyProductRepoService;

    @Mock
    private GithubService githubService;

    @Mock
    private GHOrganization organization;

    @Mock
    GHRepository repository;

    @Mock
    PagedIterable<GHTag> tags;

    @Mock
    private JsonNode dataNode;
    @Mock
    private JsonNode childNode;
    @Mock
    GHContent content = new GHContent();


    @Test
    void testExtractMavenArtifactFromJsonNode_Dependency() {
        List<MavenArtifact> artifacts = new ArrayList<>();
        boolean isDependency = true;
        String nodeName = ProductJsonConstants.DEPENDENCIES;

        createListNodeForDataNoteByName(nodeName);
        MavenArtifact mockArtifact = Mockito.mock(MavenArtifact.class);
        Mockito.doReturn(mockArtifact).when(ghAxonIvyProductRepoService).createArtifactFromJsonNode(childNode, null, isDependency);

        ghAxonIvyProductRepoService.extractMavenArtifactFromJsonNode(dataNode, isDependency, artifacts);

        assertEquals(1, artifacts.size());
        assertSame(mockArtifact, artifacts.get(0));

        isDependency = false;
        nodeName = ProductJsonConstants.PROJECTS;
        createListNodeForDataNoteByName(nodeName);

        Mockito.doReturn(mockArtifact).when(ghAxonIvyProductRepoService).createArtifactFromJsonNode(childNode, null, isDependency);

        ghAxonIvyProductRepoService.extractMavenArtifactFromJsonNode(dataNode, isDependency, artifacts);

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

        MavenArtifact artifact = ghAxonIvyProductRepoService.createArtifactFromJsonNode(dataNode, repoUrl, isDependency);

        assertEquals(repoUrl, artifact.getRepoUrl());
        assertTrue(artifact.getIsDependency());
        assertEquals(groupId, artifact.getGroupId());
        assertEquals(artifactId, artifact.getArtifactId());
        assertEquals(type, artifact.getType());
        assertTrue(artifact.getIsProductArtifact());
    }

    @Test
    void testConvertProductJsonToMavenProductInfo() throws IOException {
        assertEquals(0, ghAxonIvyProductRepoService.convertProductJsonToMavenProductInfo(null).size());
        assertEquals(0, ghAxonIvyProductRepoService.convertProductJsonToMavenProductInfo(content).size());

        InputStream inputStream = getMockInputStream();
        Mockito.when(ghAxonIvyProductRepoService.extractedContentStream(content)).thenReturn(inputStream);
        assertEquals(2, ghAxonIvyProductRepoService.convertProductJsonToMavenProductInfo(content).size());
        Mockito.when(ghAxonIvyProductRepoService.extractedContentStream(content)).thenReturn(inputStream);

        inputStream = getMockInputStreamWithOutProjectAndDependency();
        Mockito.when(ghAxonIvyProductRepoService.extractedContentStream(content)).thenReturn(inputStream);
        assertEquals(0, ghAxonIvyProductRepoService.convertProductJsonToMavenProductInfo(content).size());
    }

    private static InputStream getMockInputStream() {
        String jsonContent = "{\n" +
                "  \"installers\": [\n" +
                "    {\n" +
                "      \"data\": {\n" +
                "        \"repositories\": [\n" +
                "          {\n" +
                "            \"url\": \"http://example.com/repo\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"projects\": [\n" +
                "          {\n" +
                "            \"groupId\": \"com.example\",\n" +
                "            \"artifactId\": \"example-artifact\",\n" +
                "            \"type\": \"jar\"\n" +
                "          }\n" +
                "        ],\n" +
                "        \"dependencies\": [\n" +
                "          {\n" +
                "            \"groupId\": \"com.example.dependency\",\n" +
                "            \"artifactId\": \"dependency-artifact\",\n" +
                "            \"type\": \"jar\"\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
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

    @Test()
    void testExtractedContentStream() {
        assertNull(ghAxonIvyProductRepoService.extractedContentStream(null));
        assertNull(ghAxonIvyProductRepoService.extractedContentStream(content));
    }

    @Test()
    void testGetOrganization() {
        try {
            Mockito.when(githubService.getOrganization(Mockito.anyString())).thenReturn(organization);
            assertEquals(organization,ghAxonIvyProductRepoService.getOrganization());
            assertEquals(organization,ghAxonIvyProductRepoService.getOrganization());
        } catch (IOException e) {
            fail("Can not get default organization from Market");
        }
    }

    @Test
    void testGetAllTagsFromRepoName() {
        List<GHTag> tagList = List.of(new GHTag());
        try {
            Mockito.when(githubService.getOrganization(Mockito.anyString())).thenReturn(organization);
            Mockito.when(organization.getRepository(Mockito.anyString())).thenReturn(repository);
            Mockito.when(repository.listTags()).thenReturn(tags);
            Mockito.when(tags.toList()).thenReturn(tagList);
            List<GHTag> result = ghAxonIvyProductRepoService.getAllTagsFromRepoName("portal");
            assertEquals(tagList.size(),result.size());
            assertEquals(tagList.get(0), result.get(0));
        } catch (IOException e) {
            fail("Can not get default organization from Market");
        }

    }

}