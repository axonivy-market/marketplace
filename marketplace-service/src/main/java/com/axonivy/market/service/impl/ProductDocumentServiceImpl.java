package com.axonivy.market.service.impl;

import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductDocumentMeta;
import com.axonivy.market.factory.MavenArtifactFactory;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.repository.ProductDocumentMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.service.ProductDocumentService;
import com.axonivy.market.util.VersionUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Log4j2
@AllArgsConstructor
@Service
public class ProductDocumentServiceImpl implements ProductDocumentService {
  final ProductRepository productRepo;
  final ProductDocumentMetaRepository productDocumentMetaRepo;
  final FileDownloadService fileDownloadService;

  @Override
  public void syncDocumentForProduct(String productId, boolean isResetSync) {
    productRepo.findById(productId).ifPresent(product -> {
      List<MavenArtifact> docArtifacts = Optional.ofNullable(product.getArtifacts()).orElse(List.of())
          .stream().filter(artifact -> BooleanUtils.isTrue(artifact.getDoc())).toList();
      List<String> releasedVersions = Optional.ofNullable(product.getReleasedVersions()).orElse(List.of())
          .stream().filter(version -> VersionUtils.isValidFormatReleasedVersion(version)).toList();
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

  private void syncDocumentationForProduct(String productId, boolean isResetSync, MavenArtifact artifact,
      List<String> releasedVersions) {
    for (var version : releasedVersions) {
      List<ProductDocumentMeta> documentMetas =
          productDocumentMetaRepo.findByProductIdAndGroupIdAndArtifactIdAndVersion(
              productId, artifact.getGroupId(), artifact.getArtifactId(), version);
      if (!isResetSync && ObjectUtils.isNotEmpty(documentMetas)) {
        continue;
      }

      String downloadDocUrl = MavenArtifactFactory.buildDownloadUrlByVersion(artifact, version);
      String location = downloadDocAndUnzipToShareFolder(downloadDocUrl, isResetSync);
      if (StringUtils.isNoneBlank(location)) {
        // Remove all old records
        productDocumentMetaRepo.deleteAll(documentMetas);
        var documentMeta = new ProductDocumentMeta();
        documentMeta.setProductId(productId);
        documentMeta.setGroupId(artifact.getGroupId());
        documentMeta.setArtifactId(artifact.getArtifactId());
        documentMeta.setVersion(version);
        documentMeta.setStorageDirectory(location);
        productDocumentMetaRepo.save(documentMeta);
      }
    }
  }

  private String downloadDocAndUnzipToShareFolder(String downloadDocUrl, boolean isResetSync) {
    try {
      return fileDownloadService.downloadAndUnzipFile(downloadDocUrl, isResetSync);
    } catch (Exception e) {
      log.error("Cannot download doc", e);
    }
    return EMPTY;
  }
}
