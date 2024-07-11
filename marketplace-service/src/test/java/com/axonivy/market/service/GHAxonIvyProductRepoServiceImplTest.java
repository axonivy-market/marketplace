package com.axonivy.market.service;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.constants.ReadmeConstants;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.github.service.impl.GHAxonIvyProductRepoServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
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
import static org.mockito.Mockito.*;

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
        when(gitHubService.getOrganization(any())).thenReturn(mockGHOrganization);
        when(mockGHOrganization.getRepository(any())).thenReturn(ghRepository);
    }

	@AfterEach
	void after() throws IOException {
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
		Mockito.doReturn(mockArtifact).when(axonivyProductRepoServiceImpl).createArtifactFromJsonNode(childNode, null,
				isDependency);

		axonivyProductRepoServiceImpl.extractMavenArtifactFromJsonNode(dataNode, isDependency, artifacts);

		assertEquals(1, artifacts.size());
		assertSame(mockArtifact, artifacts.get(0));

		isDependency = false;
		nodeName = ProductJsonConstants.PROJECTS;
		createListNodeForDataNoteByName(nodeName);

		Mockito.doReturn(mockArtifact).when(axonivyProductRepoServiceImpl).createArtifactFromJsonNode(childNode, null,
				isDependency);

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

		MavenArtifact artifact = axonivyProductRepoServiceImpl.createArtifactFromJsonNode(dataNode, repoUrl,
				isDependency);

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
		assertEquals("Test README", result.getDescription().get("en"));
		assertEquals("Demo content", result.getDemo());
		assertEquals("Setup content (https://raw.githubusercontent.com/image.png)", result.getSetup());
	}

	@Test
	void testGetReadmeAndProductContentFromTag_ImageFromFolder() throws IOException {
		String readmeContentWithImageFolder = "#Product-name\n Test README\n## Demo\nDemo content\n## Setup\nSetup content (images/image.png)";

		GHContent mockImageFile = mock(GHContent.class);
		when(mockImageFile.getName()).thenReturn(ReadmeConstants.IMAGES, IMAGE_NAME);
		when(mockImageFile.isDirectory()).thenReturn(true);
		when(mockImageFile.getDownloadUrl()).thenReturn(IMAGE_DOWNLOAD_URL);

		PagedIterable<GHContent> pagedIterable = mock(PagedIterable.class);
		when(mockImageFile.listDirectoryContent()).thenReturn(pagedIterable);
		when(pagedIterable.toList()).thenReturn(List.of(mockImageFile));

		String updatedReadme = axonivyProductRepoServiceImpl.updateImagesWithDownloadUrl(List.of(mockImageFile),
				readmeContentWithImageFolder);

		assertEquals(
				"#Product-name\n Test README\n## Demo\nDemo content\n## Setup\nSetup content (https://raw.githubusercontent.com/image.png)",
				updatedReadme);
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
		String jsonContent = "{\n" + "  \"installers\": [\n" + "    {\n" + "      \"data\": {\n"
				+ "        \"repositories\": [\n" + "          {\n"
				+ "            \"url\": \"http://example.com/repo\"\n" + "          }\n" + "        ]\n" + "      }\n"
				+ "    }\n" + "  ]\n" + "}";
		return new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8));
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
		when(mockProductJson.getName()).thenReturn(ProductJsonConstants.PRODUCT_JSON_FILE);
		InputStream mockProductJsonInputStream = mock(InputStream.class);
		when(mockProductJson.read()).thenReturn(mockProductJsonInputStream);
		when(mockProductJsonInputStream.readAllBytes()).thenReturn(PRODUCT_ROOT_TREE.getBytes());
		return mockProductJson;
	}

	private static GHContent createMockReadme(String readmeContentString) throws IOException {
		GHContent mockReadme = mock(GHContent.class);
		when(mockReadme.isFile()).thenReturn(true);
		when(mockReadme.getName()).thenReturn(ReadmeConstants.README_FILE);
		InputStream mockReadmeInputStream = mock(InputStream.class);
		when(mockReadme.read()).thenReturn(mockReadmeInputStream);
		when(mockReadmeInputStream.readAllBytes()).thenReturn(readmeContentString.getBytes());
		return mockReadme;
	}
}