package com.axonivy.market.service.impl;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.bo.DownloadOption;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.DirectoryConstants;
import com.axonivy.market.entity.ExternalDocumentMeta;
import com.axonivy.market.entity.Product;
import com.axonivy.market.factory.VersionFactory;
import com.axonivy.market.repository.ArtifactRepository;
import com.axonivy.market.repository.ExternalDocumentMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.ExternalDocumentService;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.VersionUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Log4j2
@AllArgsConstructor
@Service
public class ExternalDocumentServiceImpl implements ExternalDocumentService {

  private static final String DOC_URL_PATTERN = "/%s/index.html";
  private static final String MS_WIN_SEPARATOR = "\\\\";
  final ProductRepository productRepo;
  final ExternalDocumentMetaRepository externalDocumentMetaRepo;
  final FileDownloadService fileDownloadService;
  final ArtifactRepository artifactRepo;

  @Override
  @Transactional
  public void syncDocumentForProduct(String productId, boolean isResetSync) {
    Optional.ofNullable(productRepo.findProductByIdAndRelatedData(productId)).ifPresent(product -> {
      List<Artifact> docArtifacts = fetchDocArtifacts(product.getArtifacts());

      List<String> releasedVersions = product.getReleasedVersions().stream()
          .filter(VersionUtils::isValidFormatReleasedVersion)
          .distinct()
          .toList();

      if (ObjectUtils.isEmpty(docArtifacts) || ObjectUtils.isEmpty(releasedVersions)) return;

      if (isResetSync) externalDocumentMetaRepo.deleteByProductIdAndVersionIn(productId, releasedVersions);

      Set<ExternalDocumentMeta> externalDocumentMetas = new HashSet<>();
      for (Artifact artifact : docArtifacts) {
        List<ExternalDocumentMeta> newDocs = createDocumentationForProduct(productId, isResetSync, artifact,
            releasedVersions);
        externalDocumentMetas.addAll(newDocs);
      }
      externalDocumentMetaRepo.saveAll(externalDocumentMetas);
    });
  }

  @Override
  public List<Product> findAllProductsHaveDocument() {
    return productRepo.findAllProductsHaveDocument();
  }

  @Override
  public ExternalDocumentMeta findExternalDocument(String productId, String version) {
    var product = productRepo.findById(productId);
    if (product.isEmpty()) {
      return null;
    }
    List<ExternalDocumentMeta> docMetas = externalDocumentMetaRepo.findByProductId(productId);
    List<String> docMetaVersion = docMetas.stream().map(ExternalDocumentMeta::getVersion).toList();
    String resolvedVersion = VersionFactory.get(docMetaVersion, version);
    return docMetas.stream().filter(meta -> StringUtils.equals(meta.getVersion(), resolvedVersion))
        .findAny().orElse(null);
  }

  private List<ExternalDocumentMeta> createDocumentationForProduct(String productId, boolean isResetSync,
      Artifact artifact, List<String> releasedVersions) {
    List<ExternalDocumentMeta> docMetas = externalDocumentMetaRepo.findByProductIdAndVersionIn(productId,
        releasedVersions);

    if (docMetas.isEmpty()) {
      return releasedVersions.stream()
          .map(version -> createDocumentMeta(productId, artifact, version, isResetSync))
          .filter(Objects::nonNull)
          .toList();
    }

    return docMetas.stream()
        .map(ExternalDocumentMeta::getVersion)
        .filter(version -> !releasedVersions.contains(version))
        .map(version -> createDocumentMeta(productId, artifact, version, isResetSync))
        .filter(Objects::nonNull)
        .toList();
  }


  private ExternalDocumentMeta createDocumentMeta(String productId, Artifact artifact, String version,
      boolean isResetSync) {
    String downloadDocUrl = MavenUtils.buildDownloadUrl(artifact, version);
    String location = downloadDocAndUnzipToShareFolder(downloadDocUrl, isResetSync);
    if (StringUtils.isBlank(location)) return null;

    String locationRelative = location.substring(location.indexOf(DirectoryConstants.CACHE_DIR));
    locationRelative = RegExUtils.replaceAll(String.format(DOC_URL_PATTERN, locationRelative), MS_WIN_SEPARATOR,
        CommonConstants.SLASH);

    return ExternalDocumentMeta.builder()
        .productId(productId)
        .artifactId(artifact.getArtifactId())
        .artifactName(artifact.getName())
        .version(version)
        .storageDirectory(location)
        .relativeLink(locationRelative)
        .build();
  }

  private List<Artifact> fetchDocArtifacts(List<Artifact> artifacts) {
    List<String> artifactIds = artifacts.stream().map(Artifact::getId).collect(Collectors.toList());
    List<Artifact> allArtifacts = artifactRepo.findAllByIdInAndFetchArchivedArtifacts(artifactIds);
    return allArtifacts.stream().filter(artifact -> BooleanUtils.isTrue(artifact.getDoc())).toList();
  }

  private String downloadDocAndUnzipToShareFolder(String downloadDocUrl, boolean isResetSync) {
    try {
      return fileDownloadService.downloadAndUnzipFile(downloadDocUrl,
          DownloadOption.builder().isForced(isResetSync).build());
    } catch (HttpClientErrorException e) {
      log.error("Cannot download doc {}", e.getStatusCode());
    } catch (Exception e) {
      log.error("Exception during unzip");
    }
    return EMPTY;
  }
}
