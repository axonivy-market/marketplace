package com.axonivy.market.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.kohsuke.github.PagedIterable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.kohsuke.github.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.github.service.impl.GHAxonIvyProductRepoServiceImpl;

@ExtendWith(MockitoExtension.class)
class GHAxonIvyProductRepoServiceImplTest {

    private static final String DUMMY_TAG = "v1.0.0";
    public static final String REPO_NAME = "Docker";
    public static final String RELEASE_TAG = "v10.0.0";
    public static final String DUMMY_README_CONTENT = "# Test README\n## Demo\nDemo content\n## Setup\nSetup content";
    public static final String IMAGE_NAME = "image.png";
    public static final String IMAGE_WITH_FOLDER = "(images/image.png)";
    public static final String PRODUCT_ROOT_TREE = "{ \"installers\": [{ \"id\": \"maven-dependency\", \"data\": { \"dependencies\": [{ \"groupId\": \"com.test\", \"artifactId\": \"test-artifact\", \"type\": \"iar\" }] } }] }";
    public static final String SLASH = "/";

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
    void testContentFromGHRepoAndTag() {
        var result = axonivyProductRepoServiceImpl.getContentFromGHRepoAndTag("", null, null);
        assertEquals(null, result);
    }

    @Test
    void testGetReadmeAndProductContentsFromTag() throws IOException {
        String readmeContentString = "#Product-name\n Test README\n## Demo\nDemo content\n## Setup\nSetup content (images/image.png)";
        String productJsonString = "{ \"installers\": [{ \"id\": \"maven-dependency\", \"data\": { \"dependencies\": [{ \"groupId\": \"com.test\", \"artifactId\": \"test-artifact\", \"type\": \"iar\" }] } }] }";
        after();
        when(githubService.getRepository(any())).thenReturn(ghRepository);
        GHContent mockContent = mock(GHContent.class);
        when(mockContent.isDirectory()).thenReturn(true);
        when(mockContent.getName()).thenReturn("docuware-connector-product");

        GHContent mockProductJson = mock(GHContent.class);
        when(mockProductJson.isFile()).thenReturn(true);
        when(mockProductJson.getName()).thenReturn("product.json");
        InputStream mockProductJsonInputStream = mock(InputStream.class);
        when(mockProductJson.read()).thenReturn(mockProductJsonInputStream);
        when(mockProductJsonInputStream.readAllBytes()).thenReturn(productJsonString.getBytes());

        GHContent mockReadme = mock(GHContent.class);
        when(mockReadme.isFile()).thenReturn(true);
        when(mockReadme.getName()).thenReturn("README.md");
        InputStream mockReadmeInputStream = mock(InputStream.class);
        when(mockReadme.read()).thenReturn(mockReadmeInputStream);
        when(mockReadmeInputStream.readAllBytes()).thenReturn(readmeContentString.getBytes());

        GHContent mockImageFolder = mock(GHContent.class);
        when(mockImageFolder.isDirectory()).thenReturn(true);
        when(mockImageFolder.getName()).thenReturn("images", "image.png");
        when(mockImageFolder.isFile()).thenReturn(true);
        when(mockImageFolder.getDownloadUrl()).thenReturn("https://raw.githubusercontent.com/image.png");

        // Mocking paged iterable for mockImageFolder
        PagedIterable<GHContent> mockImageFolderPagedIterable = mock(PagedIterable.class);
        when(mockImageFolder.listDirectoryContent()).thenReturn(mockImageFolderPagedIterable);
        when(mockImageFolderPagedIterable.toList()).thenReturn(Arrays.asList(mockImageFolder));

        // Mocking repository behavior
        when(ghRepository.getDirectoryContent(SLASH, RELEASE_TAG))
                .thenReturn(List.of(mockContent, mockProductJson, mockReadme, mockImageFolder));

        // Mocking paged iterable for mockContent
        PagedIterable<GHContent> mockContentPagedIterable = mock(PagedIterable.class);
        when(mockContent.listDirectoryContent()).thenReturn(mockContentPagedIterable);
        when(mockContentPagedIterable.toList()).thenReturn(Arrays.asList(mockProductJson, mockReadme));


        var result = axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag(REPO_NAME, RELEASE_TAG);

        assertEquals(RELEASE_TAG, result.getTag());
        assertTrue(result.getIsDependency());
        assertEquals("com.test", result.getGroupId());
        assertEquals("test-artifact", result.getArtifactId());
        assertEquals("iar", result.getType());
        assertEquals("Test Artifact", result.getName());
        assertEquals("Test README", result.getDescription());
        assertEquals("Demo content", result.getDemo());
        assertEquals("Setup content (https://raw.githubusercontent.com/image.png)", result.getSetup());
    }

    @Test
    void testGetReadmeAndProductContents4FromTag_ImageFromFolder() throws IOException {
        // Prepare mocks with README content containing image directive
        String readmeContentString = "#Product-name\nTest README\n## Demo\nDemo content\n## Setup\nSetup content (images/image.png)";

        GHContent mockImageFolder = mock(GHContent.class);
        GHContent mockImageFile = mock(GHContent.class);

        when(mockImageFolder.isDirectory()).thenReturn(true);
        when(mockImageFolder.getName()).thenReturn("images");

        when(mockImageFile.isFile()).thenReturn(true);
        when(mockImageFile.getName()).thenReturn("image.png");
        when(mockImageFile.getDownloadUrl()).thenReturn("https://raw.githubusercontent.com/image.png");

        // Mocking repository behavior
        when(ghRepository.getDirectoryContent(SLASH, RELEASE_TAG))
                .thenReturn(Collections.singletonList(mockImageFolder));

        // Mocking paged iterable for mockImageFolder
        PagedIterable<GHContent> mockImageFolderPagedIterable = mock(PagedIterable.class);
        when(mockImageFolder.listDirectoryContent()).thenReturn(mockImageFolderPagedIterable);
        when(mockImageFolderPagedIterable.iterator()).thenReturn((PagedIterator<GHContent>) Arrays.asList(mockImageFile).iterator());

        // Execute the method and assert results
        String updatedReadme = axonivyProductRepoServiceImpl.updateImagesWithDownloadUrl(Collections.singletonList(mockImageFolder), readmeContentString);
        assertEquals("#Product-name\nTest README\n## Demo\nDemo content\n## Setup\nSetup content (https://raw.githubusercontent.com/image.png)", updatedReadme);
    }

    @Test
    void testGetReadmeAndProductConten44tsFromTag_ImageFromFolder() throws IOException {
        // Example README.md content with an image directive
        String readmeContentString = "#Product-name\n Test README\n## Demo\nDemo content\n## Setup\nSetup content (images/image.png)";

        // Mocking the GitHub content for the image file
        GHContent mockImageFile = mock(GHContent.class);
        when(mockImageFile.isFile()).thenReturn(true);
        when(mockImageFile.getName()).thenReturn("image.png");
        when(mockImageFile.getDownloadUrl()).thenReturn("https://raw.githubusercontent.com/image.png");

        // Call the method under test to get the updated readme content
        String updatedReadme = axonivyProductRepoServiceImpl.updateImagesWithDownloadUrl(Collections.singletonList(mockImageFile), readmeContentString);

        // Assert that the image directive in the README.md content has been replaced correctly
        assertEquals("#Product-name\n Test README\n## Demo\nDemo content\n## Setup\nSetup content (https://raw.githubusercontent.com/image.png)", updatedReadme);
    }


    @Test
    void testGetReadmeAndProductContentsFromTag_WithNoFullyThreeParts() throws IOException {
        String readmeContentString = "#Product-name\n Test README\n## Setup\nSetup content";
        after();
        when(githubService.getRepository(any())).thenReturn(ghRepository);
        GHContent mockContent = mock(GHContent.class);
        when(mockContent.isDirectory()).thenReturn(true);
        when(mockContent.getName()).thenReturn("amazon-comprehend-connector-product");

        GHContent mockReadme = mock(GHContent.class);
        when(mockReadme.isFile()).thenReturn(true);
        when(mockReadme.getName()).thenReturn("README.md");
        InputStream mockReadmeInputStream = mock(InputStream.class);
        when(mockReadme.read()).thenReturn(mockReadmeInputStream);
        when(mockReadmeInputStream.readAllBytes()).thenReturn(readmeContentString.getBytes());

        when(ghRepository.getDirectoryContent("/", RELEASE_TAG)).thenReturn(List.of(mockContent));

        PagedIterable<GHContent> pagedIterable = mock(PagedIterable.class);
        when(mockContent.listDirectoryContent()).thenReturn(pagedIterable);
        when(pagedIterable.toList()).thenReturn(Arrays.asList(mockReadme));

        var result = axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag(REPO_NAME, RELEASE_TAG);
        assertNull(result.getArtifactId());
        assertEquals("Setup content", result.getSetup());
    }

    @Test
    void testGetReadmeAndProductContentsFromTag_SwitchPartsPosition() throws IOException {
        String readmeContentString = "#Product-name\n Test README\n## Setup\nSetup content\n## Demo\nDemo content";
        after();
        when(githubService.getRepository(any())).thenReturn(ghRepository);
        GHContent mockContent = mock(GHContent.class);
        when(mockContent.isDirectory()).thenReturn(true);
        when(mockContent.getName()).thenReturn("amazon-comprehend-connector-product");

        GHContent mockReadme = mock(GHContent.class);
        when(mockReadme.isFile()).thenReturn(true);
        when(mockReadme.getName()).thenReturn("README.md");
        InputStream mockReadmeInputStream = mock(InputStream.class);
        when(mockReadme.read()).thenReturn(mockReadmeInputStream);
        when(mockReadmeInputStream.readAllBytes()).thenReturn(readmeContentString.getBytes());

        when(ghRepository.getDirectoryContent("/", RELEASE_TAG)).thenReturn(List.of(mockContent));

        PagedIterable<GHContent> pagedIterable = mock(PagedIterable.class);
        when(mockContent.listDirectoryContent()).thenReturn(pagedIterable);
        when(pagedIterable.toList()).thenReturn(Arrays.asList(mockReadme));

        var result = axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag(REPO_NAME, RELEASE_TAG);
        assertEquals("Demo content", result.getDemo());
        assertEquals("Setup content", result.getSetup());
    }

    //TODO
    @Test
    void testGetReadmeAndProductContentsFromTag_ThrowException_WithNoFiles() throws IOException {
//        after();
//        when(githubService.getRepository(any())).thenReturn(ghRepository);
//        GHContent mockContent = mock(GHContent.class);
//        when(mockContent.isDirectory()).thenReturn(true);
//        when(mockContent.getName()).thenReturn("docuware-connector-product");
//
//        when(ghRepository.getDirectoryContent(SLASH, RELEASE_TAG)).thenReturn(List.of(mockContent));
//
//        PagedIterable<GHContent> pagedIterable = mock(PagedIterable.class);
//        when(mockContent.listDirectoryContent()).thenReturn(pagedIterable);
//        when(pagedIterable.toList()).thenReturn(Collections.emptyList());
//
////        var result = axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag(REPO_NAME, RELEASE_TAG);
//        assertThrows(Exception.class, () -> axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag(REPO_NAME, RELEASE_TAG));
    }
}
