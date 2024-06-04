package com.axonivy.market.factory;

import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.github.model.Meta;
import com.axonivy.market.model.Product;
import com.axonivy.market.utils.LatestVersionComparator;
import com.axonivy.market.utils.XmlReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHContent;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;


@Log4j2
public class ProductFactory {

    public static final String META_FILE = "meta.json";
    public static final String LOGO_FILE = "logo.png";


    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Product mappingByGHContent(Product product, GHContent content) {
        var contentName = content.getName();
        try {
            if (contentName.endsWith(META_FILE)) {
                ProductFactory.mappingByMetaJson(product, content);
            }

            if (contentName.endsWith(LOGO_FILE)) {
                product.setLogoUrl(content.getDownloadUrl());
            }
        } catch (IOException e) {
            log.warn("Mapping from GHContent failed", e);
        }
        return product;
    }

    public static Product mappingByMetaJson(Product product, GHContent ghContent)
            throws IOException {
        var meta = jsonDecode(ghContent);
        if (meta == null) {
            return product;
        }
        // log.warn(meta);
        product.setKey(meta.getId());
        product.setName(meta.getName());
        product.setListed(meta.getListed());
        product.setType(meta.getType());
        product.setTags(meta.getTags());
        product.setVersion(meta.getVersion());
        product.setShortDescript(meta.getDescription());
        product.setVendor(StringUtils.isBlank(meta.getVendor()) ? "Axon Ivy AG" : meta.getVendor());
        product.setVendorImage(
                StringUtils.isBlank(meta.getVendorImage()) ? "/images/misc/axonivy-logo-black.svg" : meta.getVendor());
        product.setVendorUrl(StringUtils.isBlank(meta.getVendorUrl()) ? "https://www.axonivy.com" : meta.getVendorUrl());
        product.setPlatformReview(StringUtils.isBlank(meta.getPlatformReview()) ? "4.0" : meta.getPlatformReview());
        product.setSourceUrl(meta.getSourceUrl());
        product.setStatusBadgeUrl(meta.getStatusBadgeUrl());
        product.setLanguage(meta.getLanguage());
        product.setIndustry(meta.getIndustry());
        product.setMavenArtifacts(meta.getMavenArtifacts());
        product.setVersions(getVersionFromMaven(product, true));
        // TODO mapping default data
        // product.setCost() = ghContent->cost ?? 'Free';
        // product.setCompatibility(meta.get) = ghContent->compatibility ?? '';
        // product.setValidate(meta.get) = ghContent->validate ?? false;
        // product.setContactUs(meta) = ghContent->contactUs ?? false;
        return product;
    }

    public static Meta jsonDecode(GHContent ghContent) throws IOException {
        if (ghContent != null) {
            return MAPPER.readValue(ghContent.read().readAllBytes(), Meta.class);
        }
        return null;
    }

    public static List<String> getVersionFromMaven(Product product, boolean isShowDevVersion) {
        List<MavenArtifact> productArtifact = getMavenArtifacts(product.getMavenArtifacts());
        Set<String> versions = new HashSet<>();
        for (MavenArtifact artifact : productArtifact) {
            versions.addAll(getVersionFromArtifactInfo(artifact.getRepoUrl(), artifact.getGroupId(), artifact.getArtifactId()));
            Optional.ofNullable(artifact.getArchivedArtifacts()).orElse(Collections.emptyList()).forEach(archivedArtifact -> versions.addAll(getVersionFromArtifactInfo(artifact.getRepoUrl(), archivedArtifact.getGroupId(), archivedArtifact.getArtifactId())));
        }
        List<String> versionList = new ArrayList<>(versions);
        versionList.sort(new LatestVersionComparator());
        Stream<String> versionStream = versionList.stream();
        if (isShowDevVersion) {
            return versionStream.filter(version -> isReleasedVersionOrUnReleaseDevVersion(versionList, version)).sorted(new LatestVersionComparator()).toList();
        }
        return versionStream.filter(ProductFactory::isReleaseVersion).sorted(new LatestVersionComparator()).toList();
    }

    private static String buildMavenMetadataUrlFromArtifact(String repoUrl, String groupId, String artifactID) {
        if (StringUtils.isAnyBlank(groupId, artifactID)) {
            return StringUtils.EMPTY;
        }
        repoUrl = Optional.ofNullable(repoUrl).orElse(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);
        groupId = groupId.replace(MavenConstants.GROUP_ID_SEPARATOR, MavenConstants.GROUP_ID_URL_SEPARATOR);
        return String.format(MavenConstants.METADATA_URL_FORMAT, repoUrl, groupId, artifactID);
    }

    public static List<MavenArtifact> getMavenArtifacts(List<MavenArtifact> artifacts) {
        return Optional.ofNullable(artifacts).orElse(Collections.emptyList()).stream().filter(product -> !product.getArtifactId().endsWith(MavenConstants.PRODUCT_ARTIFACT_POSTFIX)).toList();
    }

    private static List<String> getVersionFromArtifactInfo(String repoUrl, String groupId, String artifactID) {
        List<String> versions = new ArrayList<>();
        String baseUrl = buildMavenMetadataUrlFromArtifact(repoUrl, groupId, artifactID);
        if (StringUtils.isNotBlank(baseUrl)) {
            versions.addAll(XmlReader.readXMLFromUrl(baseUrl));
        }
        return versions;
    }

    private static boolean isReleaseVersion(String version) {
        return !(isSprintVersion(version) || isSnapshotVersion(version));
    }

    private static boolean isSprintVersion(String version) {
        return version.contains(MavenConstants.SPRINT_RELEASE_POSTFIX);
    }

    private static boolean isSnapshotVersion(String version) {
        return version.endsWith(MavenConstants.SNAPSHOT_RELEASE_POSTFIX);
    }

    private static boolean isReleasedVersionOrUnReleaseDevVersion(List<String> versions, String version) {
        if (isSnapshotVersion(version)) {
            return !versions.contains(version.replace(MavenConstants.SNAPSHOT_RELEASE_POSTFIX, StringUtils.EMPTY));
        } else if (isSprintVersion(version)) {
            return !versions.contains(version.split(MavenConstants.SPRINT_RELEASE_POSTFIX)[0]);
        }
        return true;
    }

    private boolean isDevVersionNotRelease(List<String> versions, String version) {
        return true;
    }

}
