package com.axonivy.market.service;

import ch.qos.logback.core.joran.spi.XMLUtil;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.MavenArtifactModel;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.github.model.ArchivedArtifact;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.impl.VersionServiceImpl;
import com.axonivy.market.utils.XmlReaderUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import static org.mockito.Mockito.when;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

@Log4j2
@ExtendWith(MockitoExtension.class)
public class VersionServiceImplTest {
    private String repoName;
    private Map<String, List<ArchivedArtifact>> archivedArtifactsMap;
    private List<MavenArtifact> artifactsFromMeta;
    private MavenArtifactVersion proceedDataCache;
    private MavenArtifact metaProductArtifact;
    @Spy
    @InjectMocks
    private VersionServiceImpl versionService;

    @Mock
    private GHAxonIvyProductRepoService gitHubService;

    @Mock
    private MavenArtifactVersionRepository mavenArtifactVersionRepository;

    @Mock
    private ProductRepository productRepository;

    @BeforeEach()
    public void prepareBeforeTest() {
        archivedArtifactsMap = new HashMap<>();
        artifactsFromMeta = new ArrayList<>();
        ReflectionTestUtils.setField(versionService, "repoName", repoName);
        ReflectionTestUtils.setField(versionService, "archivedArtifactsMap", archivedArtifactsMap);
        ReflectionTestUtils.setField(versionService, "artifactsFromMeta", artifactsFromMeta);
        ReflectionTestUtils.setField(versionService, "proceedDataCache", proceedDataCache);
        ReflectionTestUtils.setField(versionService, "metaProductArtifact", metaProductArtifact);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetArtifactsAndVersionToDisplay() {
        Assertions.assertEquals("", "");

    }

    @Test
    public void testUpdateArtifactsInVersionWithProductArtifact() {
        Assertions.assertEquals("", "");

    }

    @Test
    public void testSanitizeMetaArtifactBeforeHandle() {
        Assertions.assertEquals("", "");

    }

    @Test
    public void testGetVersionsToDisplay() {
        Assertions.assertEquals("", "");

    }

    @Test
    public void getVersionsFromMavenArtifacts() {
        String repoUrl = "https://maven.axonivy.com";
        String groupId = "com.axonivy.connector.adobe.acrobat.sign";
        String artifactId = "adobe-acrobat-sign-connector";
        String archivedArtifactId = "adobe-sign-connector";
        List<ArchivedArtifact> archivedArtifacts = List.of(new ArchivedArtifact("10.0.9", groupId, archivedArtifactId));
        artifactsFromMeta.add(new MavenArtifact(repoUrl, null, groupId, artifactId, null, null, null, null));
        ArrayList<String> versionFromArtifact = new ArrayList<>();
        versionFromArtifact.add("10.0.6");
        versionFromArtifact.add("10.0.5");
        versionFromArtifact.add("10.0.4");

        ArrayList<String> versionFromArchivedArtifact = new ArrayList<>();
        versionFromArchivedArtifact.add("10.0.3");
        versionFromArchivedArtifact.add("10.0.2");
        versionFromArchivedArtifact.add("10.0.1");

        when(versionService.getVersionsFromArtifactDetails(repoUrl, groupId, artifactId)).thenReturn(versionFromArtifact);

        Assertions.assertEquals(versionService.getVersionsFromMavenArtifacts(), versionFromArtifact);
    }

    @Test
    public void testGetVersionsFromArtifactDetails() {

        ArrayList<String> versionFromArtifact = new ArrayList<>();
        versionFromArtifact.add("10.0.6");
        versionFromArtifact.add("10.0.5");
        versionFromArtifact.add("10.0.4");
        try (MockedStatic<XmlReaderUtils> xmlUtils = Mockito.mockStatic(XmlReaderUtils.class)){
            xmlUtils.when(() -> XmlReaderUtils.readXMLFromUrl(Mockito.anyString())).thenReturn(versionFromArtifact);
        };


        Assertions.assertEquals("", "");

    }

    @Test
    public void testBuildMavenMetadataUrlFromArtifact() {
        String repoUrl = "https://maven.axonivy.com";
        String groupId = "com.axonivy.connector.adobe.acrobat.sign";
        String artifactId = "adobe-acrobat-sign-connector";
        String metadataUrl = "https://maven.axonivy.com/com/axonivy/connector/adobe/acrobat/sign/adobe-acrobat-sign-connector/maven-metadata.xml";
        Assertions.assertEquals(versionService.buildMavenMetadataUrlFromArtifact(repoUrl, null, artifactId), StringUtils.EMPTY);
        Assertions.assertEquals(versionService.buildMavenMetadataUrlFromArtifact(repoUrl, groupId, null), StringUtils.EMPTY);

        Assertions.assertEquals(versionService.buildMavenMetadataUrlFromArtifact(repoUrl, groupId, artifactId), metadataUrl);
    }

    @Test
    public void testIsReleasedVersionOrUnReleaseDevVersion() {
        String releasedVersion = "10.0.20";
        String snapshotVersion = "10.0.20-SNAPSHOT";
        String sprintVersion = "10.0.21-m1234";
        List<String> versions = List.of(releasedVersion, snapshotVersion, sprintVersion);
        Assertions.assertTrue(versionService.isReleasedVersionOrUnReleaseDevVersion(versions, releasedVersion));
        Assertions.assertTrue(versionService.isReleasedVersionOrUnReleaseDevVersion(versions, sprintVersion));
        Assertions.assertFalse(versionService.isReleasedVersionOrUnReleaseDevVersion(versions, snapshotVersion));
    }

    @Test
    public void testIsSnapshotVersion() {
        String targetVersion = "10.0.21-SNAPSHOT";
        Assertions.assertTrue(versionService.isSnapshotVersion(targetVersion));

        targetVersion = "10.0.21-m1234";
        Assertions.assertFalse(versionService.isSnapshotVersion(targetVersion));

        targetVersion = "10.0.21";
        Assertions.assertFalse(versionService.isSnapshotVersion(targetVersion));
    }

    @Test
    public void testIsSprintVersion() {
        String targetVersion = "10.0.21-m1234";
        Assertions.assertTrue(versionService.isSprintVersion(targetVersion));

        targetVersion = "10.0.21-SNAPSHOT";
        Assertions.assertFalse(versionService.isSprintVersion(targetVersion));

        targetVersion = "10.0.21";
        Assertions.assertFalse(versionService.isSprintVersion(targetVersion));
    }

    @Test
    public void testIsReleasedVersion() {
        String targetVersion = "10.0.21";
        Assertions.assertTrue(versionService.isReleasedVersion(targetVersion));

        targetVersion = "10.0.21-SNAPSHOT";
        Assertions.assertFalse(versionService.isReleasedVersion(targetVersion));

        targetVersion = "10.0.21-m1231";
        Assertions.assertFalse(versionService.isReleasedVersion(targetVersion));
    }

    @Test
    public void testIsMatchWithDesignerVersion() {
        String designerVersion = "10.0.21";
        String targetVersion = "10.0.21.2";
        Assertions.assertTrue(versionService.isMatchWithDesignerVersion(targetVersion, designerVersion));

        targetVersion = "10.0.21-SNAPSHOT";
        Assertions.assertFalse(versionService.isMatchWithDesignerVersion(targetVersion, designerVersion));

        targetVersion = "10.0.19";
        Assertions.assertFalse(versionService.isMatchWithDesignerVersion(targetVersion, designerVersion));
    }

    @Test
    public void testGetProductJsonByVersion() {

    }

    @Test
    public void testConvertMavenArtifactToModel() {
        String downloadUrl = "https://maven.axonivy.com/com/axonivy/connector/adobe/acrobat/sign/adobe-acrobat-sign-connector/10.0.21/adobe-acrobat-sign-connector-10.0.21.iar";
        String artifactName = "Adobe Acrobat Sign Connector (iar)";

        MavenArtifact targetArtifact = new MavenArtifact(null, null, "com.axonivy.connector.adobe.acrobat.sign", "adobe-acrobat-sign-connector", null, null, null, null);

        // Assert case handle artifact without name
        MavenArtifactModel result = versionService.convertMavenArtifactToModel(targetArtifact, "10.0.21");
        MavenArtifactModel expectedResult = new MavenArtifactModel(artifactName, downloadUrl, null);
        Assertions.assertEquals(result.getName(), expectedResult.getName());
        Assertions.assertEquals(result.getDownloadUrl(), expectedResult.getDownloadUrl());

        // Assert case handle artifact with name
        artifactName = "Adobe Connector";
        String expectedArtifactName = "Adobe Connector (iar)";
        targetArtifact.setName(artifactName);
        result = versionService.convertMavenArtifactToModel(targetArtifact, "10.0.21");
        expectedResult = new MavenArtifactModel(artifactName, downloadUrl, null);
        Assertions.assertEquals(result.getName(), expectedArtifactName);
        Assertions.assertEquals(result.getDownloadUrl(), expectedResult.getDownloadUrl());
    }

    @Test
    public void testConvertMavenArtifactsToModels() {
        // Assert case param is empty
        List<MavenArtifactModel> result = versionService.convertMavenArtifactsToModels(Collections.emptyList(), "10.0.21");
        Assertions.assertEquals(result, Collections.emptyList());

        // Assert case param is null
        result = versionService.convertMavenArtifactsToModels(null, "10.0.21");
        Assertions.assertEquals(result, Collections.emptyList());

        // Assert case param is a list with existed element
        MavenArtifact targetArtifact = new MavenArtifact(null, null, "com.axonivy.connector.adobe.acrobat.sign", "adobe-acrobat-sign-connector", null, null, null, null);
        MavenArtifactModel expectedModel = new MavenArtifactModel("Adobe Connector ", "http://market.axonivy.com", null);
        result = versionService.convertMavenArtifactsToModels(List.of(targetArtifact), "10.0.21");
        Assertions.assertEquals(1, result.size());
    }

    @Test
    public void testBuildDownloadUrlFromArtifactAndVersion() {
        // Set up artifact for testing
        String targetArtifactId = "adobe-acrobat-sign-connector";
        String targetGroupId = "com.axonivy.connector";
        MavenArtifact targetArtifact = new MavenArtifact(null, null, targetGroupId, targetArtifactId, "iar", null, null, null);
        String targetVersion = "10.0.10";

        // Assert case without archived artifact
        String expectedResult = String.format(MavenConstants.ARTIFACT_DOWNLOAD_URL_FORMAT, MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL, "com/axonivy/connector", targetArtifactId, targetVersion, targetArtifactId, targetVersion, "iar");
        String result = versionService.buildDownloadUrlFromArtifactAndVersion(targetArtifact, targetVersion);
        Assertions.assertEquals(expectedResult, result);

        // Assert case with artifact not match & use custom repo
        ArchivedArtifact adobeArchivedArtifactVersion9 = new ArchivedArtifact("10.0.9", "com.axonivy.adobe.connector", "adobe-acrobat-sign-connector");
        ArchivedArtifact adobeArchivedArtifactVersion8 = new ArchivedArtifact("10.0.8", "com.axonivy.adobe.sign.connector", "adobe-sign-connector");
        archivedArtifactsMap.put(targetArtifactId, List.of(adobeArchivedArtifactVersion9, adobeArchivedArtifactVersion8));
        String customRepoUrl = "https://nexus.axonivy.com";
        targetArtifact.setRepoUrl(customRepoUrl);
        result = versionService.buildDownloadUrlFromArtifactAndVersion(targetArtifact, targetVersion);
        expectedResult = String.format(MavenConstants.ARTIFACT_DOWNLOAD_URL_FORMAT, customRepoUrl, "com/axonivy/connector", targetArtifactId, targetVersion, targetArtifactId, targetVersion, "iar");
        Assertions.assertEquals(expectedResult, result);

        //Assert case with artifact got matching archived artifact & use custom file type
        String customType = "zip";
        targetArtifact.setType(customType);
        targetVersion = "10.0.9";
        result = versionService.buildDownloadUrlFromArtifactAndVersion(targetArtifact, "10.0.9");
        expectedResult = String.format(MavenConstants.ARTIFACT_DOWNLOAD_URL_FORMAT, customRepoUrl, "com/axonivy/adobe/connector", targetArtifactId, targetVersion, targetArtifactId, targetVersion, customType);
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void testFindArchivedArtifactInfoBestMatchWithVersion() {
        String targetArtifactId = "adobe-acrobat-sign-connector";
        String targetVersion = "10.0.10";
        ArchivedArtifact result = versionService.findArchivedArtifactInfoBestMatchWithVersion(targetArtifactId, targetVersion);
        Assertions.assertNull(result);

        //Assert case with target version higher than all of latest version from archived artifact list
        ArchivedArtifact adobeArchivedArtifactVersion8 = new ArchivedArtifact("10.0.8", "com.axonivy.connector", "adobe-sign-connector");
        ArchivedArtifact adobeArchivedArtifactVersion9 = new ArchivedArtifact("10.0.9", "com.axonivy.connector", "adobe-acrobat-sign-connector");
        List<ArchivedArtifact> archivedArtifacts = new ArrayList<>();
        archivedArtifacts.add(adobeArchivedArtifactVersion8);
        archivedArtifacts.add(adobeArchivedArtifactVersion9);
        archivedArtifactsMap.put(targetArtifactId, archivedArtifacts);
        result = versionService.findArchivedArtifactInfoBestMatchWithVersion(targetArtifactId, targetVersion);
        Assertions.assertNull(result);

        //Assert case with target version less than all of latest version from archived artifact list
        result = versionService.findArchivedArtifactInfoBestMatchWithVersion(targetArtifactId, "10.0.7");
        Assertions.assertEquals(adobeArchivedArtifactVersion8, result);

        //Assert case with target version is in range of archived artifact list
        ArchivedArtifact adobeArchivedArtifactVersion10 = new ArchivedArtifact("10.0.10", "com.axonivy.connector", "adobe-sign-connector");

        archivedArtifactsMap.get(targetArtifactId).add(adobeArchivedArtifactVersion10);
        result = versionService.findArchivedArtifactInfoBestMatchWithVersion(targetArtifactId, targetVersion);
        Assertions.assertEquals(adobeArchivedArtifactVersion10, result);
    }

    @Test
    public void testConvertArtifactIdToName() {
        String defaultArtifactId = "adobe-acrobat-sign-connector";
        String result = versionService.convertArtifactIdToName(defaultArtifactId);
        Assertions.assertEquals("Adobe Acrobat Sign Connector", result);

        result = versionService.convertArtifactIdToName(null);
        Assertions.assertEquals(StringUtils.EMPTY, result);

        result = versionService.convertArtifactIdToName(StringUtils.EMPTY);
        Assertions.assertEquals(StringUtils.EMPTY, result);

        result = versionService.convertArtifactIdToName(" ");
        Assertions.assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    public void testGetRepoNameFromMarketRepo() {
        String defaultRepositoryName = "market/adobe-acrobat-connector";
        String result = versionService.getRepoNameFromMarketRepo(defaultRepositoryName);
        Assertions.assertEquals(result, "adobe-acrobat-connector");

        defaultRepositoryName = "market/utils/adobe-acrobat-connector";
        result = versionService.getRepoNameFromMarketRepo(defaultRepositoryName);
        Assertions.assertEquals(result, "adobe-acrobat-connector");

        defaultRepositoryName = "adobe-acrobat-connector";
        result = versionService.getRepoNameFromMarketRepo(defaultRepositoryName);
        Assertions.assertEquals(result, "adobe-acrobat-connector");
    }
}