package com.axonivy.market.service.impl;

import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.model.MavenArtifactModel;
import com.axonivy.market.service.VersionService;
import com.axonivy.market.utils.LatestVersionComparator;
import com.axonivy.market.utils.XmlReader;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHContent;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

@Service
public class VersionServiceImpl implements VersionService {
    private final GHAxonIvyProductRepoService gitHubService;

    public VersionServiceImpl(GHAxonIvyProductRepoService gitHubService) {
        this.gitHubService = gitHubService;
    }

    public static List<MavenArtifact> getMavenArtifacts(List<MavenArtifact> artifacts) {
        return Optional.ofNullable(artifacts).orElse(Collections.emptyList()).stream().filter(product -> !product.getArtifactId().endsWith(MavenConstants.PRODUCT_ARTIFACT_POSTFIX)).toList();
    }

    @Override
    public List<String> getVersionsToDisplay(List<MavenArtifact> artifacts, Boolean isShowDevVersion, String designerVersion) throws IOException {
        List<String> versions = getVersionsFromMavenArtifacts(artifacts);
        Stream<String> versionStream = versions.stream();
        if (BooleanUtils.isTrue(isShowDevVersion)) {
            return versionStream.filter(version -> isReleasedVersionOrUnReleaseDevVersion(versions, version)).sorted(new LatestVersionComparator()).toList();
        }
        if (StringUtils.isNotBlank(designerVersion)) {
            return versionStream.filter(version -> isMatchWithDesignerVersion(version, designerVersion)).toList();
        }
        return versionStream.filter(this::isReleasedVersion).sorted(new LatestVersionComparator()).toList();
    }

    private List<String> getVersionsFromMavenArtifacts(List<MavenArtifact> artifacts) {
        Set<String> versions = new HashSet<>();
        for (MavenArtifact artifact : artifacts) {
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
            versions.addAll(XmlReader.readXMLFromUrl(baseUrl));
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

    //    @Override
    public Map<String, List<MavenArtifactModel>> getArtifactsAndVersionToDisplay(String productId, Boolean isShowDevVersion, String designerVersion) throws IOException {
        //TODO  convert productID to reponame;
        String repoName = productId;

        //TODO: get mavent artifact from meta.json


        List<MavenArtifact> artifactsFromMeta = Collections.emptyList();
        List<String> versionsToDisplay = getVersionsToDisplay(artifactsFromMeta, isShowDevVersion, designerVersion);
        Map<String, List<MavenArtifactModel>> result = new HashMap<>();
        List<MavenArtifactModel> additionalArtifacts = new ArrayList<>();
        for (String version : versionsToDisplay) {
            List<MavenArtifactModel> models = new ArrayList<>();
            List<MavenArtifact> artifacts = getProductJsonByVersion(repoName, version);
            models.addAll(convertMavenArtifactsToModels(artifacts, version));
            models.addAll(additionalArtifacts);
            result.put(version, models);
        }
        return result;
    }

    private List<MavenArtifact> getProductJsonByVersion(String repoName, String version) throws IOException {
        String productJsonFilePath = repoName + "-product/product.json";
        GHContent productJsonContent = gitHubService.getContentFromGHRepoAndTag(repoName, productJsonFilePath, version);
        return gitHubService.convertProductJsonToMavenProductInfo(productJsonContent);
    }

    private List<MavenArtifactModel> convertMavenArtifactsToModels(List<MavenArtifact> artifacts, String version) {
        List<MavenArtifactModel> result = new ArrayList<>();
        for (MavenArtifact artifact : artifacts) {
            result.add(convertMavenArtifactToModel(artifact, version));
        }
        return result;
    }

    private MavenArtifactModel convertMavenArtifactToModel(MavenArtifact artifact, String version) {
        return new MavenArtifactModel(artifact.getName(), buildDownloadUrlFromArtifactAndVersion(artifact, version));
    }

    private String buildDownloadUrlFromArtifactAndVersion(MavenArtifact artifact, String version) {
        String repoUrl = Optional.ofNullable(artifact.getRepoUrl()).orElse(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);
        String groupId = artifact.getGroupId().replace(MavenConstants.GROUP_ID_SEPARATOR, MavenConstants.GROUP_ID_URL_SEPARATOR);
        return String.format(MavenConstants.ARTIFACT_DOWNLOAD_URL_FORMAT, repoUrl, groupId, artifact.getArtifactId(), version, artifact.getArtifactId(), version, artifact.getType());
    }

}