package com.axonivy.market.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.axonivy.market.model.ReadmeModel;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axonivy.market.github.service.GithubService;
import com.axonivy.market.github.service.impl.GHAxonIvyProductRepoServiceImpl;

@Log4j2
@ExtendWith(MockitoExtension.class)
class GHAxonIvyProductRepoServiceImplTest {

    private static final String DUMMY_TAG = "v1.0.0";

    @Mock
    PagedIterable<GHTag> listTags;
    @Mock
    PagedIterable<GHContent> listContents;
    @Mock
    GHRepository ghRepository;

    @Mock
    GHContent ghContent;

    @Mock
    GithubService githubService;

    @InjectMocks
    private GHAxonIvyProductRepoServiceImpl axonivyProductRepoServiceImpl;

    @BeforeEach
    void setup() throws IOException {
        var mockGHOrganization = mock(GHOrganization.class);
        when(mockGHOrganization.getRepository(any())).thenReturn(ghRepository);
        when(githubService.getOrganization(any())).thenReturn(mockGHOrganization);
        when(githubService.getRepository(any())).thenReturn(ghRepository);
    }
//
//    @Test
//    void testAllTagsFromRepoName() throws IOException {
//        var mockTag = mock(GHTag.class);
//        when(mockTag.getName()).thenReturn(DUMMY_TAG);
//        when(listTags.toList()).thenReturn(List.of(mockTag));
//        when(ghRepository.listTags()).thenReturn(listTags);
//        var result = axonivyProductRepoServiceImpl.getAllTagsFromRepoName("");
//        assertEquals(1, result.size());
//        assertEquals(DUMMY_TAG, result.get(0).getName());
//    }
//
//    @Test
//    void testContentFromGHRepoAndTag() {
//        var result = axonivyProductRepoServiceImpl.getContentFromGHRepoAndTag("", null, null);
//        assertEquals(null, result);
//    }

    @Test
    public void testGetReadmeAndProductContentsFromTag_ValidValues() throws IOException {
        String repoName = "Docker";
        String tag = "v10.0.0";
        String readmeContentString = "# Test README\n## Demo\nDemo content\n## Setup\nSetup content";
        String productJsonString = "{ \"installers\": [{ \"id\": \"maven-dependency\", \"data\": { \"dependencies\": [{ \"groupId\": \"com.test\", \"artifactId\": \"test-artifact\", \"type\": \"jar\" }] } }] }";

        GHContent readmeContent = mock(GHContent.class);
        GHContent productJsonContent = mock(GHContent.class);

        when(ghRepository.getDirectoryContent("/", tag)).thenReturn(List.of(readmeContent, productJsonContent));

        log.error("readme {}", readmeContent);
        when(readmeContent.isFile()).thenReturn(true);
        when(readmeContent.getName()).thenReturn("README.md");
        log.error("readme {}", readmeContent.getName());
        log.error("readme {}", readmeContent.listDirectoryContent());
        log.error("readme {}", readmeContent.getPath());
        when(readmeContent.read().readAllBytes()).thenReturn(readmeContentString.getBytes());

        when(productJsonContent.isFile()).thenReturn(true);
        when(productJsonContent.getName()).thenReturn("product.json");
        when(productJsonContent.read()).thenReturn(new ByteArrayInputStream(productJsonString.getBytes(StandardCharsets.UTF_8)));

        ReadmeModel result = axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag(repoName, tag);

        assertEquals(tag, result.getTag());
        Assertions.assertTrue(result.getIsDependency());
        assertEquals("com.test", result.getGroupId());
        assertEquals("test-artifact", result.getArtifactId());
        assertEquals("jar", result.getType());
        assertEquals("Test Artifact", result.getName());
        assertEquals("Test README", result.getDescription());
        assertEquals("Demo content", result.getDemo());
        assertEquals("Setup content", result.getSetup());
    }

    @Test
    public void testGetReadmeAndProductContentsFromTag() throws IOException {
        String repoName = "Docker";
        String tag = "v10.0.0";
        String readmeContentString = "# Test README\n## Demo\nDemo content\n## Setup\nSetup content";
        String productJsonString = "{ \"installers\": [{ \"id\": \"maven-dependency\", \"data\": { \"dependencies\": [{ \"groupId\": \"com.test\", \"artifactId\": \"test-artifact\", \"type\": \"iar\" }] } }] }";
        GHContent mockContent = mock(GHContent.class);
//        when(githubService.getRepository(repoName)).thenReturn(ghRepository);
//        log.error("repo {}", githubService.getRepository(repoName));
        when(ghRepository.getDirectoryContent("/", tag)).thenReturn(List.of(mockContent));
        when(mockContent.getName()).thenReturn("docuware-connector-product");
        when(mockContent.listDirectoryContent()).thenReturn(listContents);
        for (GHContent listContent : listContents) {

        }
//        when(mockContent.listDirectoryContent()).thenReturn(List.of(mockContent));
//        log.error("readmeContents.listDirectoryContent() {}", readmeContents.listDirectoryContent());
        when(mockContent.isFile()).thenReturn(true);
        when(mockContent.getName()).thenReturn("README.md").thenReturn("product.json");
        log.error("name {}", mockContent.getName());
        log.error("readmeContent name {}", mockContent.getDownloadUrl());
        InputStream mockInputStream = mock(InputStream.class);
        when(mockContent.read()).thenReturn(mockInputStream);
        when(mockInputStream.readAllBytes()).thenReturn(readmeContentString.getBytes()).thenReturn(productJsonString.getBytes());
        log.error("readmeContent content {}", mockContent.read().readAllBytes());


//        when(readmeContents.getName()).thenReturn("README.md");
//        log.error("name {}", readmeContent.getName());
//        when(readmeContent.read().readAllBytes()).thenReturn(readmeContentString);
//        log.error("readmeContent content {}", readmeContent.read().readAllBytes());
//        GHContent productJsonContent = mock(GHContent.class);

//log.error("json content {}", productJsonContent.read().readAllBytes());
//        when(githubService.getRepository(repoName)).thenReturn(ghRepository);
//        when(ghRepository.getDirectoryContent("/", tag)).thenReturn(List.of(readmeContent, productJsonContent));
//log.error("dir {}", ghRepository.getDirectoryContent("/", tag));
        var result = axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag(repoName, tag);
        log.error("result {}", result);
//log.error("result1 {}", result.getGroupId());
        assertEquals(tag, result.getTag());
//        assertTrue(result.getIsDependency());
//        assertEquals("com.test", result.getGroupId());
//        assertEquals("test-artifact", result.getArtifactId());
//        assertEquals("jar", result.getType());
//        assertEquals("Test Artifact", result.getName());
        assertEquals("Test README", result.getDescription());
        assertEquals("Demo content", result.getDemo());
        assertEquals("Setup content", result.getSetup());
    }
    @Test
    public void abc() throws IOException {
        String repoName = "Docker";
        String tag = "v10.0.0";
        String readmeContentString = "# Test README\n## Demo\nDemo content\n## Setup\nSetup content";
        String productJsonString = "{ \"installers\": [{ \"id\": \"maven-dependency\", \"data\": { \"dependencies\": [{ \"groupId\": \"com.test\", \"artifactId\": \"test-artifact\", \"type\": \"iar\" }] } }] }";

        ReadmeModel result = axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag(repoName, tag);
    }

//    private List<GHContent> mockGhContentList() {
//        GHContent ghContent1 = new GHContent();
//        ghContent1.setName("docuware-connector-product");
//    }
    @Test
    public void testGetReadmeAndProductContentsFromTag_WithValidRepoAndTag() throws IOException {
        String repoName = "Docker";
        String tag = "v10.0.0";
        List<GHContent> contents = new ArrayList<>();
        contents.add(ghContent);
        when(githubService.getRepository(repoName)).thenReturn(ghRepository);
        when(ghRepository.getDirectoryContent("/", "v10.0.0")).thenReturn(contents);
        when(ghContent.isDirectory()).thenReturn(true);
        when(ghContent.getName().endsWith("-product")).thenReturn(true);
        when(ghContent.listDirectoryContent()).thenReturn(listContents);
        when(ghContent.isFile()).thenReturn(true);
        when(ghContent.getName()).thenReturn("README.md");
        when(ghContent.read()).thenReturn(new ByteArrayInputStream("#Hi\n\n".getBytes()));

        var result = axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag("Hi", "v10.0.0");
        assertEquals("ur business processes.", result.getDescription());
    }

    @Test
    public void testGetReadmeAndProductContentsFromTag_WithNoReadmeFile() throws IOException {
        List<GHContent> contents = new ArrayList<>();
        log.error("dir {}", ghRepository.getDirectoryContent("/", "v10.0.0"));
        contents.add(ghContent);
        when(githubService.getRepository("Docker")).thenReturn(ghRepository);
        log.error("repo {}", githubService.getRepository("Docker"));
        log.error("dir1 {}", ghRepository.getDirectoryContent("/", "v10.0.0"));
        when(ghRepository.getDirectoryContent("/", "v10.0.0")).thenReturn(contents);
        when(ghContent.isDirectory()).thenReturn(true);
        log.error("name {}", ghContent.getName());
        when(ghContent.getName().endsWith("-product")).thenReturn(true);
        when(ghContent.listDirectoryContent()).thenReturn(listContents);
        when(ghContent.isFile()).thenReturn(false);

        var result = axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag("Docker", "v10.0.0");
        assertEquals("", result.getDescription());
    }

    //
//    @Test
//    public void testGetProductJsonContent_NoProductJsonFilePresent() throws IOException {
//        // Setup
//        ArrayList<GHContent> contents = new ArrayList<>();
//        ReadmeModel readmeModel = new ReadFooModel();
//
//        // Execute yourClass.getProductJsonContent(readmeModel, contents);
//
//        // Verify assertNull(readmeModel.getGroupId());
//    }
//
    @Test
    public void testGetExtractedPartsOfReadme_WithSectionMarkers() {
        String readmeContent = "Description\n## Setup\nSetup Details\n## Demo\nDemo Details";
        ReadmeModel readmeModel = new ReadmeModel();

        axonivyProductRepoServiceImpl.getExtractedPartsOfReadme(readmeModel, readmeContent);
        assertEquals("Description", readmeModel.getDescription());
        assertEquals("Setup Details", readmeModel.getSetup());
        assertEquals("Demo Details", readmeModel.getDemo());
    }
//
//    @Test
//    void getReadmeAndProductContentsFromTag() {
//    }
}
