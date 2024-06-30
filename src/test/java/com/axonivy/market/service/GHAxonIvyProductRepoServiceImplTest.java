package com.axonivy.market.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.ReleaseTagConstants;
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
    public static final String RELEASE_TAG = "v10.0.0";
    public static final String IMAGE_NAME = "image.png";
    public static final String PRODUCT_ROOT_TREE = "{ \"installers\": [{ \"id\": \"maven-dependency\", \"data\": { \"dependencies\": [{ \"groupId\": \"com.test\", \"artifactId\": \"test-artifact\", \"type\": \"iar\" }] } }] }";
    public static final String DOCUWARE_CONNECTOR_PRODUCT = "docuware-connector-product";
    public static final String IMAGE_DOWNLOAD_URL = "https://raw.githubusercontent.com/image.png";

    @Mock
    PagedIterable<GHTag> listTags;

    @Mock
    GHRepository ghRepository;

    @Mock
    GitHubService gitHubService;

    GHOrganization mockGHOrganization = mock(GHOrganization.class);

    @InjectMocks
    private GHAxonIvyProductRepoServiceImpl axonivyProductRepoServiceImpl;

    @BeforeEach
    void setup() throws IOException {
        when(mockGHOrganization.getRepository(any())).thenReturn(ghRepository);
        when(gitHubService.getOrganization(any())).thenReturn(mockGHOrganization);
    }

    @AfterEach
    void after() throws IOException {
        reset(mockGHOrganization);
        reset(gitHubService);
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
        String readmeContentWithImage = "#Product-name\n Test README\n## Demo\nDemo content\n## Setup\nSetup content (image.png)";

        GHContent mockContent = createMockProductFolder();

        GHContent mockProductJson = createMockProductJson();

        GHContent mockReadme = createMockReadme(readmeContentWithImage);

        GHContent mockImage = mock(GHContent.class);
        when(mockImage.getName()).thenReturn(IMAGE_NAME);
        when(mockImage.isFile()).thenReturn(true);
        when(mockImage.getDownloadUrl()).thenReturn(IMAGE_DOWNLOAD_URL);

        when(ghRepository.getDirectoryContent(CommonConstants.SLASH, RELEASE_TAG))
                .thenReturn(List.of(mockContent, mockProductJson, mockReadme));

        PagedIterable<GHContent> mockContentPagedIterable = mock(PagedIterable.class);
        when(mockContent.listDirectoryContent()).thenReturn(mockContentPagedIterable);
        when(mockContentPagedIterable.toList()).thenReturn(List.of(mockProductJson, mockReadme, mockImage));

        var result = axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag(ghRepository, RELEASE_TAG);

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
    void testGetReadmeAndProductContentFromTag_ImageFromFolder() throws IOException {
        String readmeContentWithImageFolder = "#Product-name\n Test README\n## Demo\nDemo content\n## Setup\nSetup content (images/image.png)";

        GHContent mockImageFile = mock(GHContent.class);
        when(mockImageFile.getName()).thenReturn(ReleaseTagConstants.IMAGES, IMAGE_NAME);
        when(mockImageFile.isDirectory()).thenReturn(true);
        when(mockImageFile.getDownloadUrl()).thenReturn(IMAGE_DOWNLOAD_URL);

        PagedIterable<GHContent> pagedIterable = mock(PagedIterable.class);
        when(mockImageFile.listDirectoryContent()).thenReturn(pagedIterable);
        when(pagedIterable.toList()).thenReturn(List.of(mockImageFile));

        String updatedReadme = axonivyProductRepoServiceImpl.updateImagesWithDownloadUrl(List.of(mockImageFile), readmeContentWithImageFolder);

        assertEquals("#Product-name\n Test README\n## Demo\nDemo content\n## Setup\nSetup content (https://raw.githubusercontent.com/image.png)", updatedReadme);
    }

    @Test
    void testGetReadmeAndProductContentsFromTag_WithNoFullyThreeParts() throws IOException {
        String readmeContentString = "#Product-name\n Test README\n## Setup\nSetup content";

        GHContent mockContent = createMockProductFolder();

        GHContent mockReadme = createMockReadme(readmeContentString);

        when(ghRepository.getDirectoryContent(CommonConstants.SLASH, RELEASE_TAG)).thenReturn(List.of(mockContent));

        PagedIterable<GHContent> pagedIterable = mock(PagedIterable.class);
        when(mockContent.listDirectoryContent()).thenReturn(pagedIterable);
        when(pagedIterable.toList()).thenReturn(List.of(mockReadme));

        var result = axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag(ghRepository, RELEASE_TAG);

        assertNull(result.getArtifactId());
        assertEquals("Setup content", result.getSetup());
    }

    @Test
    void testGetReadmeAndProductContentsFromTag_SwitchPartsPosition() throws IOException {
        String readmeContentString = "#Product-name\n Test README\n## Setup\nSetup content\n## Demo\nDemo content";

        GHContent mockContent = createMockProductFolder();

        GHContent mockReadme = createMockReadme(readmeContentString);

        when(ghRepository.getDirectoryContent(CommonConstants.SLASH, RELEASE_TAG)).thenReturn(List.of(mockContent));

        PagedIterable<GHContent> pagedIterable = mock(PagedIterable.class);
        when(mockContent.listDirectoryContent()).thenReturn(pagedIterable);
        when(pagedIterable.toList()).thenReturn(List.of(mockReadme));

        var result = axonivyProductRepoServiceImpl.getReadmeAndProductContentsFromTag(ghRepository, RELEASE_TAG);
        assertEquals("Demo content", result.getDemo());
        assertEquals("Setup content", result.getSetup());
    }

    private GHContent createMockProductFolder() throws IOException {
        when(gitHubService.getRepository(any())).thenReturn(ghRepository);
        GHContent mockContent = mock(GHContent.class);
        when(mockContent.isDirectory()).thenReturn(true);
        when(mockContent.getName()).thenReturn(DOCUWARE_CONNECTOR_PRODUCT);
        return mockContent;
    }

    private static GHContent createMockProductJson() throws IOException {
        GHContent mockProductJson = mock(GHContent.class);
        when(mockProductJson.isFile()).thenReturn(true);
        when(mockProductJson.getName()).thenReturn(ReleaseTagConstants.PRODUCT_JSON_FILE);
        InputStream mockProductJsonInputStream = mock(InputStream.class);
        when(mockProductJson.read()).thenReturn(mockProductJsonInputStream);
        when(mockProductJsonInputStream.readAllBytes()).thenReturn(PRODUCT_ROOT_TREE.getBytes());
        return mockProductJson;
    }

    private static GHContent createMockReadme(String readmeContentString) throws IOException {
        GHContent mockReadme = mock(GHContent.class);
        when(mockReadme.isFile()).thenReturn(true);
        when(mockReadme.getName()).thenReturn(ReleaseTagConstants.README_FILE);
        InputStream mockReadmeInputStream = mock(InputStream.class);
        when(mockReadme.read()).thenReturn(mockReadmeInputStream);
        when(mockReadmeInputStream.readAllBytes()).thenReturn(readmeContentString.getBytes());
        return mockReadme;
    }
}
