package com.axonivy.market.service.impl;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.constants.NonStandardProductPPackageConstants;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Product;
import com.axonivy.market.github.model.ArchivedArtifact;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.entity.MavenArtifactModel;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.VersionService;
import com.axonivy.market.comparator.ArchivedArtifactsComparator;
import com.axonivy.market.comparator.LatestVersionComparator;
import com.axonivy.market.utils.XmlReaderUtils;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHContent;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@Service
@Getter
public class VersionServiceImpl implements VersionService {

    private final GHAxonIvyProductRepoService gitHubService;
    private final MavenArtifactVersionRepository mavenArtifactVersionRepository;
    private final ProductRepository productRepository;
    private String repoName;
    private Map<String, List<ArchivedArtifact>> archivedArtifactsMap;
    private List<MavenArtifact> artifactsFromMeta;
    private MavenArtifactVersion proceedDataCache;
    private MavenArtifact metaProductArtifact;
    private final LatestVersionComparator latestVersionComparator = new LatestVersionComparator();
    private String productJsonFilePath;
    private String productId;

    public String getProductJsonFilePath() {
        return productJsonFilePath;
    }

    public String getRepoName() {
        return repoName;
    }

    public VersionServiceImpl(GHAxonIvyProductRepoService gitHubService, MavenArtifactVersionRepository mavenArtifactVersionRepository, ProductRepository productRepository) {
        this.gitHubService = gitHubService;
        this.mavenArtifactVersionRepository = mavenArtifactVersionRepository;
        this.productRepository = productRepository;

    }

    private void resetData() {
        repoName = null;
        archivedArtifactsMap = new HashMap<>();
        artifactsFromMeta = Collections.emptyList();
        proceedDataCache = null;
        metaProductArtifact = null;
        productJsonFilePath = null;
        productId = null;

    }

    public List<MavenArtifactVersionModel> getArtifactsAndVersionToDisplay(String productId, Boolean isShowDevVersion, String designerVersion) {
        List<MavenArtifactVersionModel> result = new ArrayList<>();
        resetData();

        this.productId = productId;
        artifactsFromMeta = getProductMetaArtifacts(productId);
        List<String> versionsToDisplay = getVersionsToDisplay(isShowDevVersion, designerVersion);
        proceedDataCache = mavenArtifactVersionRepository.findById(productId).orElse(new MavenArtifactVersion(productId));
        metaProductArtifact = artifactsFromMeta.stream().filter(artifact -> artifact.getArtifactId().endsWith(MavenConstants.PRODUCT_ARTIFACT_POSTFIX)).findAny().orElse(new MavenArtifact());

        sanitizeMetaArtifactBeforeHandle();

        boolean isNewVersionDetected = handleArtifactForVersionToDisplay(versionsToDisplay, result);
        if (isNewVersionDetected) {
            mavenArtifactVersionRepository.save(proceedDataCache);
        }
        return result;
    }

    public boolean handleArtifactForVersionToDisplay(List<String> versionsToDisplay, List<MavenArtifactVersionModel> result) {
        boolean isNewVersionDetected = false;
        for (String version : versionsToDisplay) {
            List<MavenArtifactModel> artifactsInVersion = convertMavenArtifactsToModels(artifactsFromMeta, version);
            List<MavenArtifactModel> productArtifactModels = proceedDataCache.getProductArtifactWithVersionReleased().get(version);
            if (productArtifactModels == null) {
                isNewVersionDetected = true;
                productArtifactModels = updateArtifactsInVersionWithProductArtifact(version);
            }
            artifactsInVersion.addAll(productArtifactModels);
            result.add(new MavenArtifactVersionModel(version, artifactsInVersion.stream().distinct().toList()));
        }
        return isNewVersionDetected;
    }

    public List<MavenArtifactModel> updateArtifactsInVersionWithProductArtifact(String version) {
        List<MavenArtifactModel> productArtifactModels = convertMavenArtifactsToModels(getProductJsonByVersion(version), version);
        proceedDataCache.getVersions().add(version);
        proceedDataCache.getProductArtifactWithVersionReleased().put(version, productArtifactModels);
        return productArtifactModels;
    }

    public List<MavenArtifact> getProductMetaArtifacts(String productId) {
        Product productInfo = productRepository.findById(productId).orElse(new Product());
        String fullRepoName = productInfo.getRepositoryName();
        if (StringUtils.isNotEmpty(fullRepoName)) {
            repoName = getRepoNameFromMarketRepo(fullRepoName);
        }
        return Optional.ofNullable(productInfo.getArtifacts()).orElse(new ArrayList<>());
    }

    public void sanitizeMetaArtifactBeforeHandle() {
        artifactsFromMeta.remove(metaProductArtifact);
        artifactsFromMeta.forEach(artifact -> {
            List<ArchivedArtifact> archivedArtifacts = new ArrayList<>(Optional.ofNullable(artifact.getArchivedArtifacts())
                    .orElse(Collections.emptyList()).stream().sorted(new ArchivedArtifactsComparator()).toList());
            Collections.reverse(archivedArtifacts);
            archivedArtifactsMap.put(artifact.getArtifactId(), archivedArtifacts);
        });
    }

    @Override
    public List<String> getVersionsToDisplay(Boolean isShowDevVersion, String designerVersion) {
        List<String> versions = getVersionsFromMavenArtifacts();
        Stream<String> versionStream = versions.stream();
        if (BooleanUtils.isTrue(isShowDevVersion)) {
            return versionStream.filter(version -> isReleasedVersionOrUnReleaseDevVersion(versions, version)).sorted(new LatestVersionComparator()).toList();
        }
        if (StringUtils.isNotBlank(designerVersion)) {
            return versionStream.filter(version -> isMatchWithDesignerVersion(version, designerVersion)).toList();
        }
        return versions.stream().filter(this::isReleasedVersion).sorted(new LatestVersionComparator()).toList();
    }

    public List<String> getVersionsFromMavenArtifacts() {
        Set<String> versions = new HashSet<>();
        for (MavenArtifact artifact : artifactsFromMeta) {
            versions.addAll(getVersionsFromArtifactDetails(artifact.getRepoUrl(), artifact.getGroupId(), artifact.getArtifactId()));
            Optional.ofNullable(artifact.getArchivedArtifacts()).orElse(Collections.emptyList()).forEach(archivedArtifact -> versions.addAll(getVersionsFromArtifactDetails(artifact.getRepoUrl(), archivedArtifact.getGroupId(), archivedArtifact.getArtifactId())));
        }
        List<String> versionList = new ArrayList<>(versions);
        versionList.sort(new LatestVersionComparator());
        return versionList;
    }

    @Override
    public List<String> getVersionsFromArtifactDetails(String repoUrl, String groupId, String artifactID) {
        List<String> versions = new ArrayList<>();
        String baseUrl = buildMavenMetadataUrlFromArtifact(repoUrl, groupId, artifactID);
        if (StringUtils.isNotBlank(baseUrl)) {
            versions.addAll(XmlReaderUtils.readXMLFromUrl(baseUrl));
        }
        return versions;
    }

    @Override
    public String buildMavenMetadataUrlFromArtifact(String repoUrl, String groupId, String artifactID) {
        if (StringUtils.isAnyBlank(groupId, artifactID)) {
            return StringUtils.EMPTY;
        }
        repoUrl = Optional.ofNullable(repoUrl).orElse(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);
        groupId = groupId.replace(MavenConstants.GROUP_ID_SEPARATOR, MavenConstants.GROUP_ID_URL_SEPARATOR);
        return String.format(MavenConstants.METADATA_URL_FORMAT, repoUrl, groupId, artifactID);
    }

    public boolean isReleasedVersionOrUnReleaseDevVersion(List<String> versions, String version) {
        if (isSnapshotVersion(version)) {
            return !versions.contains(version.replace(MavenConstants.SNAPSHOT_RELEASE_POSTFIX, StringUtils.EMPTY));
        } else if (isSprintVersion(version)) {
            return !versions.contains(version.split(MavenConstants.SPRINT_RELEASE_POSTFIX)[0]);
        }
        return true;
    }

    public boolean isSnapshotVersion(String version) {
        return version.endsWith(MavenConstants.SNAPSHOT_RELEASE_POSTFIX);
    }

    public boolean isSprintVersion(String version) {
        return version.contains(MavenConstants.SPRINT_RELEASE_POSTFIX);
    }

    public boolean isReleasedVersion(String version) {
        return !(isSprintVersion(version) || isSnapshotVersion(version));
    }

    public boolean isMatchWithDesignerVersion(String version, String designerVersion) {
        return isReleasedVersion(version) && version.startsWith(designerVersion);
    }

    public List<MavenArtifact> getProductJsonByVersion(String version) {
        List<MavenArtifact> result = new ArrayList<>();
        String versionTag = buildProductJsonFilePath(version);
        try {
            GHContent productJsonContent = gitHubService.getContentFromGHRepoAndTag(repoName, productJsonFilePath, versionTag);
            if (Objects.isNull(productJsonContent)) {
                return result;
            }
            result = gitHubService.convertProductJsonToMavenProductInfo(productJsonContent);
        } catch (IOException e) {
            log.warn("Can not get the product.json from repo {} by path in {} version {}", repoName, productJsonFilePath, versionTag);
        }
        return result;
    }

    public String buildProductJsonFilePath(String version) {
        String verisonTag = "v" + version;
        String pathToProductJsonFileFromTagContent = metaProductArtifact.getArtifactId();
        switch (productId) {
            case NonStandardProductPPackageConstants.PORTAL:
                pathToProductJsonFileFromTagContent = "AxonIvyPortal/portal-product";
                verisonTag = version;
                break;
            case NonStandardProductPPackageConstants.CONNECTIVITY_FEATURE:
                pathToProductJsonFileFromTagContent = "connectivity/connectivity-demos-product";
                break;
            case NonStandardProductPPackageConstants.ERROR_HANDLING:
                pathToProductJsonFileFromTagContent = "error-handling/error-handling-demos-product";
                break;
            case NonStandardProductPPackageConstants.WORKFLOW_DEMO:
                pathToProductJsonFileFromTagContent = "workflow/workflow-demos-product";
                break;
            case NonStandardProductPPackageConstants.MICROSOFT_365:
                pathToProductJsonFileFromTagContent = "msgraph-connector-product/products/msgraph-connector";
                break;
            case NonStandardProductPPackageConstants.HTML_DIALOG_DEMO:
                pathToProductJsonFileFromTagContent = "html-dialog/html-dialog-demos-product";
                break;
            case NonStandardProductPPackageConstants.RULE_ENGINE_DEMOS:
                pathToProductJsonFileFromTagContent = "rule-engine/rule-engine-demos-product";
                break;
            default:
                break;
        }
        productJsonFilePath = String.format(GitHubConstants.PROUCT_JSON_FILE_PATH_FORMAT, pathToProductJsonFileFromTagContent);
        return verisonTag;
    }

    public MavenArtifactModel convertMavenArtifactToModel(MavenArtifact artifact, String version) {
        String artifactName = artifact.getName();
        if (StringUtils.isBlank(artifactName)) {
            artifactName = convertArtifactIdToName(artifact.getArtifactId());
        }
        artifact.setType(Optional.ofNullable(artifact.getType()).orElse("iar"));
        artifactName = String.format(MavenConstants.ARTIFACT_NAME_FORMAT, artifactName, artifact.getType());
        return new MavenArtifactModel(artifactName, buildDownloadUrlFromArtifactAndVersion(artifact, version), artifact.getIsProductArtifact());
    }

    public List<MavenArtifactModel> convertMavenArtifactsToModels(List<MavenArtifact> artifacts, String version) {
        List<MavenArtifactModel> list = new ArrayList<>();
        if (!CollectionUtils.isEmpty(artifacts)) {
            for (MavenArtifact artifact : artifacts) {
                MavenArtifactModel mavenArtifactModel = convertMavenArtifactToModel(artifact, version);
                list.add(mavenArtifactModel);
            }
        }
        return list;
    }

    public String buildDownloadUrlFromArtifactAndVersion(MavenArtifact artifact, String version) {
        String groupIdByVersion = artifact.getGroupId();
        String artifactIdByVersion = artifact.getArtifactId();
        String repoUrl = Optional.ofNullable(artifact.getRepoUrl()).orElse(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);
        ArchivedArtifact archivedArtifactBestMatchVersion = findArchivedArtifactInfoBestMatchWithVersion(artifact.getArtifactId(), version);

        if (Objects.nonNull(archivedArtifactBestMatchVersion)) {
            groupIdByVersion = archivedArtifactBestMatchVersion.getGroupId();
            artifactIdByVersion = archivedArtifactBestMatchVersion.getArtifactId();
        }
        groupIdByVersion = groupIdByVersion.replace(MavenConstants.GROUP_ID_SEPARATOR, MavenConstants.GROUP_ID_URL_SEPARATOR);
        return String.format(MavenConstants.ARTIFACT_DOWNLOAD_URL_FORMAT, repoUrl, groupIdByVersion, artifactIdByVersion, version, artifactIdByVersion, version, artifact.getType());
    }

    public ArchivedArtifact findArchivedArtifactInfoBestMatchWithVersion(String artifactId, String version) {
        List<ArchivedArtifact> archivedArtifacts = archivedArtifactsMap.get(artifactId);

        if (CollectionUtils.isEmpty(archivedArtifacts)) {
            return null;
        }
        for (ArchivedArtifact archivedArtifact : archivedArtifacts) {
            if (latestVersionComparator.compare(archivedArtifact.getLastVersion(), version) <= 0) {
                return archivedArtifact;
            }
        }
        return null;
    }

    public String convertArtifactIdToName(String artifactId) {
        if (StringUtils.isBlank(artifactId)) {
            return StringUtils.EMPTY;
        }
        return Arrays.stream(artifactId.split(MavenConstants.ARTIFACT_ID_SEPARATOR))
                .map(part -> part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase())
                .collect(Collectors.joining(MavenConstants.ARTIFACT_NAME_SEPARATOR));
    }

    public String getRepoNameFromMarketRepo(String fullRepoName) {
        String[] repoNamePart = fullRepoName.split("/");
        return repoNamePart[repoNamePart.length - 1];
    }
}