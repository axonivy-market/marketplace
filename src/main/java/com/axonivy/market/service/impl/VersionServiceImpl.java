package com.axonivy.market.service.impl;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Product;
import com.axonivy.market.factory.ProductFactory;
import com.axonivy.market.github.model.ArchivedArtifact;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.entity.MavenArtifactModel;
import com.axonivy.market.github.model.Meta;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.VersionService;
import com.axonivy.market.utils.ArchivedArtifactsComparator;
import com.axonivy.market.utils.LatestVersionComparator;
import com.axonivy.market.utils.XmlReaderUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHContent;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

@Log4j2
@Service
public class VersionServiceImpl implements VersionService {
    private final GHAxonIvyProductRepoService gitHubService;
    private final MavenArtifactVersionRepository mavenArtifactVersionRepository;
    private final ProductRepository productRepository;
    private String repoName;
    private final Map<String, List<ArchivedArtifact>> archivedArtifactsMap = new HashMap<>();
    private List<MavenArtifact> artifactsFromMeta;
    private MavenArtifactVersion processedDataCache;
    private MavenArtifact metaProductArtifact;
    private LatestVersionComparator comparator = new LatestVersionComparator();


    public VersionServiceImpl(GHAxonIvyProductRepoService gitHubService, MavenArtifactVersionRepository mavenArtifactVersionRepository, ProductRepository productRepository) {
        this.gitHubService = gitHubService;
        this.mavenArtifactVersionRepository = mavenArtifactVersionRepository;
        this.productRepository = productRepository;

    }

    //TODO: convert product id -> repo name
    public List<MavenArtifactVersionModel> getArtifactsAndVersionToDisplay(String productId, Boolean isShowDevVersion, String designerVersion) {
        List<MavenArtifactVersionModel> result = new ArrayList<>();
        boolean isNewVersionDetected = false;

        artifactsFromMeta = getProductMetaArtifacts(productId);
        List<String> versionsToDisplay = getVersionsToDisplay(isShowDevVersion, designerVersion);
        processedDataCache = mavenArtifactVersionRepository.findById(productId).orElse(new MavenArtifactVersion(productId));
        metaProductArtifact = artifactsFromMeta.stream().filter(artifact -> artifact.getArtifactId().endsWith(MavenConstants.PRODUCT_ARTIFACT_POSTFIX)).findAny().orElse(new MavenArtifact());

        sanitizeMetaArtifactBeforeHandle();

        for (String version : versionsToDisplay) {
            boolean isProductArtifactSynced = true;
            if (processedDataCache.getProductArtifactWithVersionReleased().get(version) == null) {
                isNewVersionDetected = true;
                isProductArtifactSynced = false;
                artifactsFromMeta.addAll(getProductJsonByVersion(version));
            }
            updateResultAndCacheData(version, result, isProductArtifactSynced);
        }
        if (isNewVersionDetected) {
            mavenArtifactVersionRepository.save(processedDataCache);
        }
        return result;
    }

    private void updateResultAndCacheData(String version, List<MavenArtifactVersionModel> result, boolean isProductArtifactSynced) {
        List<MavenArtifactModel> artifactModels = artifactsFromMeta.stream().distinct().filter(Objects::nonNull).map(artifact -> convertMavenArtifactToModel(artifact, version)).toList();
        result.add(new MavenArtifactVersionModel(version, artifactModels));
        updateCacheData(version, isProductArtifactSynced, artifactModels);
        if (!isProductArtifactSynced) {
            processedDataCache.getVersions().add(version);
            processedDataCache.getProductArtifactWithVersionReleased().put(version, artifactModels.stream().filter(MavenArtifactModel::getIsProductArtifact).toList());
        }
    }


    private void updateCacheData(String version, boolean isProductArtifactSynced, List<MavenArtifactModel> artifactModels) {
        if (!isProductArtifactSynced) {
            processedDataCache.getVersions().add(version);
            processedDataCache.getProductArtifactWithVersionReleased().put(version, artifactModels.stream().filter(MavenArtifactModel::getIsProductArtifact).toList());
        }
    }

    private List<MavenArtifact> getProductMetaArtifacts(String productId) {
        Product productInfo = productRepository.findByKey(productId);
        repoName = Optional.ofNullable(productInfo).map(Product::getRepositoryName).orElse(StringUtils.EMPTY);
        if(StringUtils.isNotEmpty(repoName)){
            
        }
        return productInfo.getArtifacts();
    }

    private void sanitizeMetaArtifactBeforeHandle() {
        artifactsFromMeta.remove(metaProductArtifact);
        artifactsFromMeta.forEach(artifact -> {
            artifact.setType(Optional.ofNullable(artifact.getType()).orElse("iar"));
            List<ArchivedArtifact> archivedArtifacts = new ArrayList<>(artifact.getArchivedArtifacts().stream().sorted(new ArchivedArtifactsComparator()).toList());
            Collections.reverse(archivedArtifacts);
            archivedArtifactsMap.put(artifact.getArtifactId(), archivedArtifacts);
        });
    }

    @Override
    public List<String> getVersionsToDisplay(Boolean isShowDevVersion, String designerVersion) {
        List<String> versions = getVersionsFromMavenArtifacts();
        Stream<String> versionStream = versions.stream();
        List<String> availableVersionsFromMaven = versions.stream().filter(this::isReleasedVersion).sorted(new LatestVersionComparator()).toList();
        if (BooleanUtils.isTrue(isShowDevVersion)) {
            return versionStream.filter(version -> isReleasedVersionOrUnReleaseDevVersion(versions, version)).sorted(new LatestVersionComparator()).toList();
        }
        if (StringUtils.isNotBlank(designerVersion)) {
            return versionStream.filter(version -> isMatchWithDesignerVersion(version, designerVersion)).toList();
        }
        return availableVersionsFromMaven;
    }

    private List<String> getVersionsFromMavenArtifacts() {
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

    private boolean isMatchWithDesignerVersion(String version, String designerVersion) {
        return isReleasedVersion(version) && version.startsWith(designerVersion);
    }

    private List<MavenArtifact> getProductJsonByVersion(String version) {
        List<MavenArtifact> result = new ArrayList<>();
        String productJsonFilePath = String.format(GitHubConstants.PROUCT_JSON_FILE_PATH_FORMAT, metaProductArtifact.getArtifactId());
        try {
            GHContent productJsonContent = gitHubService.getContentFromGHRepoAndTag(repoName, productJsonFilePath, "v" + version);
            if (Objects.isNull(productJsonContent)) {
                return result;
            }
            result = gitHubService.convertProductJsonToMavenProductInfo(productJsonContent);
        } catch (IOException e) {
            log.warn("Can not get the product.json from repo {} by path in {} version {}", repoName, productJsonFilePath, version);
        }
        return result;
    }

    private MavenArtifactModel convertMavenArtifactToModel(MavenArtifact artifact, String version) {
        String artifactName = artifact.getName();
        if (StringUtils.isBlank(artifactName)) {
            artifactName = convertArtifactIdToName(artifact.getArtifactId(), artifact.getType());
        }
        return new MavenArtifactModel(artifactName, buildDownloadUrlFromArtifactAndVersion(artifact, version), artifact.getIsProductArtifact());
    }

    private String buildDownloadUrlFromArtifactAndVersion(MavenArtifact artifact, String version) {
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

    private ArchivedArtifact findArchivedArtifactInfoBestMatchWithVersion(String artifactId, String version) {
        List<ArchivedArtifact> archivedArtifacts = archivedArtifactsMap.get(artifactId);

        if (CollectionUtils.isEmpty(archivedArtifacts)) {
            return null;
        }
        for (ArchivedArtifact archivedArtifact : archivedArtifacts) {
            if (comparator.compare(archivedArtifact.getLastVersion(), version) <= 0) {
                return archivedArtifact;
            }
        }
        return null;
    }

    private String convertArtifactIdToName(String artifactId, String type) {
        if (StringUtils.isBlank(artifactId)) {
            return StringUtils.EMPTY;
        }
        String artifactNameFromArtifactId = artifactId.replace(MavenConstants.ARTIFACT_ID_SEPARATOR, MavenConstants.ARTIFACT_NAME_SEPARATOR);
        return String.format(MavenConstants.ARTIFACT_NAME_FORMAT, artifactNameFromArtifactId, type);
    }
}