package com.axonivy.market.service.impl;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.bo.DownloadOption;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.DirectoryConstants;
import com.axonivy.market.entity.ExternalDocumentMeta;
import com.axonivy.market.entity.Product;
import com.axonivy.market.factory.VersionFactory;
import com.axonivy.market.repository.ExternalDocumentMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.ExternalDocumentService;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.VersionUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

  @Override
  public void syncDocumentForProduct(String productId, List<String> nonSyncReleasedVersions, boolean isResetSync) {
    productRepo.findById(productId).ifPresent(product -> {
      var docArtifacts = Optional.ofNullable(product.getArtifacts()).orElse(List.of())
          .stream().filter(artifact -> BooleanUtils.isTrue(artifact.getDoc())).toList();

      List<String> releasedVersions = ObjectUtils.isEmpty(nonSyncReleasedVersions)
          ? Optional.ofNullable(product.getReleasedVersions()).orElse(new ArrayList<>())
          : nonSyncReleasedVersions;
      releasedVersions = releasedVersions.stream().filter(VersionUtils::isValidFormatReleasedVersion).toList();

      if (ObjectUtils.isEmpty(docArtifacts) || ObjectUtils.isEmpty(releasedVersions)) {
        return;
      }

      for (var artifact : docArtifacts) {
        syncDocumentationForProduct(productId, isResetSync, artifact, releasedVersions);
      }
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

  private void syncDocumentationForProduct(String productId, boolean isResetSync, Artifact artifact,
      List<String> releasedVersions) {
    for (var version : releasedVersions) {
      if (isResetSync) {
        externalDocumentMetaRepo.deleteByProductIdAndVersion(productId, version);
      } else {
        if (ObjectUtils.isNotEmpty(externalDocumentMetaRepo.findByProductIdAndVersion(productId, version))) {
          continue;
        }
      }

      String downloadDocUrl = MavenUtils.buildDownloadUrl(artifact, version);
      String location = downloadDocAndUnzipToShareFolder(downloadDocUrl, isResetSync);
      if (StringUtils.isNoneBlank(location)) {
        var documentMeta = new ExternalDocumentMeta();
        documentMeta.setProductId(productId);
        documentMeta.setArtifactId(artifact.getArtifactId());
        documentMeta.setArtifactName(artifact.getName());
        documentMeta.setVersion(version);
        documentMeta.setStorageDirectory(location);
        // remove prefix 'data' and replace all ms win separator to slash if present
        var locationRelative = location.substring(location.indexOf(DirectoryConstants.CACHE_DIR));
        locationRelative = RegExUtils.replaceAll(String.format(DOC_URL_PATTERN, locationRelative), MS_WIN_SEPARATOR,
            CommonConstants.SLASH);
        documentMeta.setRelativeLink(locationRelative);
        externalDocumentMetaRepo.save(documentMeta);
      }
    }
  }

  private String downloadDocAndUnzipToShareFolder(String downloadDocUrl, boolean isResetSync) {
    try {
      return fileDownloadService.downloadAndUnzipFile(downloadDocUrl,
          DownloadOption.builder().isForced(isResetSync).build());
    } catch (HttpClientErrorException e) {
      log.error("Cannot download doc {}", e.getStatusCode());
    } catch (Exception e) {
      log.error("Exception during unzip", e);
    }
    return EMPTY;
  }
}
