package com.axonivy.market.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
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
    public static final String REPO_NAME = "Docker";
    public static final String RELEASE_TAG = "v10.0.0";
    public static final String DUMMY_README_FILE_CONTENT = "# Test README\n## Demo\nDemo content\n## Setup\nSetup content";
    public static final String PRODUCT_ROOT_TREE = "{ \"installers\": [{ \"id\": \"maven-dependency\", \"data\": { \"dependencies\": [{ \"groupId\": \"com.test\", \"artifactId\": \"test-artifact\", \"type\": \"iar\" }] } }] }";

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
    GHOrganization mockGHOrganization = mock(GHOrganization.class);
    @InjectMocks
    private GHAxonIvyProductRepoServiceImpl axonivyProductRepoServiceImpl;

    @BeforeEach
    void setup() throws IOException {
        when(mockGHOrganization.getRepository(any())).thenReturn(ghRepository);
        when(githubService.getOrganization(any())).thenReturn(mockGHOrganization);
    }

    @AfterEach
    void after() throws IOException {
        reset(mockGHOrganization);
        reset(githubService);
    }

    @Test
    void testAllTagsFromRepoName() throws IOException {
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
        var result = axonivyProductRepoServiceImpl.getContentFromGHRepoAndTag("", null, null);
        assertEquals(null, result);
    }

    @Test
    public void testGetReadmeAndProductContentsFromTag() throws IOException {
        String repoName = "Docker";
        String tag = "v10.0.0";
        String readmeContentString = "#Product-name\n Test README\n## Demo\nDemo content\n## Setup\nSetup content";
        String productJsonString = "{ \"installers\": [{ \"id\": \"maven-dependency\", \"data\": { \"dependencies\": [{ \"groupId\": \"com.test\", \"artifactId\": \"test-artifact\", \"type\": \"iar\" }] } }] }";
        after();
        when(githubService.getRepository(any())).thenReturn(ghRepository);
        // Mocking the directory content
        GHContent mockContent = mock(GHContent.class);
        when(mockContent.isDirectory()).thenReturn(true);
        when(mockContent.getName()).thenReturn("docuware-connector-product");

        // Mocking the product.json file
        GHContent mockProductJson = mock(GHContent.class);
        when(mockProductJson.isFile()).thenReturn(true);
        when(mockProductJson.getName()).thenReturn("product.json");
        InputStream mockProductJsonInputStream = mock(InputStream.class);
        when(mockProductJson.read()).thenReturn(mockProductJsonInputStream);
        when(mockProductJsonInputStream.readAllBytes()).thenReturn(productJsonString.getBytes());

        // Mocking the README.md file
        GHContent mockReadme = mock(GHContent.class);
        when(mockReadme.isFile()).thenReturn(true);
        when(mockReadme.getName()).thenReturn("README.md");
        InputStream mockReadmeInputStream = mock(InputStream.class);
        when(mockReadme.read()).thenReturn(mockReadmeInputStream);
        when(mockReadmeInputStream.readAllBytes()).thenReturn(readmeContentString.getBytes());

        when(ghRepository.getDirectoryContent("/", tag)).thenReturn(List.of(mockContent));

        PagedIterable<GHContent> pagedIterable = mock(PagedIterable.class);
        when(mockContent.listDirectoryContent()).thenReturn(pagedIterable);
        when(pagedIterable.toList()).thenReturn(Arrays.asList(mockProductJson, mockReadme));

        var result = axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag(repoName, tag);

        assertEquals(tag, result.getTag());
        assertTrue(result.getIsDependency());
        assertEquals("com.test", result.getGroupId());
        assertEquals("test-artifact", result.getArtifactId());
        assertEquals("iar", result.getType());
        assertEquals("Test Artifact", result.getName());
        assertEquals("Test README", result.getDescription());
        assertEquals("Demo content", result.getDemo());
        assertEquals("Setup content", result.getSetup());
    }

//    @Test
//    public void testGetReadmeAndProductContentsFromTag_WithNoReadmeFile() throws IOException {
//        List<GHContent> contents = new ArrayList<>();
//        contents.add(ghContent);
//        when(githubService.getRepository("Docker")).thenReturn(ghRepository);
//        when(ghRepository.getDirectoryContent("/", "v10.0.0")).thenReturn(contents);
//        when(ghContent.isDirectory()).thenReturn(true);
//        when(ghContent.getName().endsWith("-product")).thenReturn(true);
//        when(ghContent.listDirectoryContent()).thenReturn(listContents);
//        when(ghContent.isFile()).thenReturn(false);
//
//        var result = axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag("Docker", "v10.0.0");
//        assertEquals("", result.getDescription());
//    }

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

}
