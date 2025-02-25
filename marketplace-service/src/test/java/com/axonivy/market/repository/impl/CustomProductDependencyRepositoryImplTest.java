package com.axonivy.market.repository.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.ProductDependency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomProductDependencyRepositoryImplTest extends BaseSetup {
  @Mock
  MongoTemplate mongoTemplate;
  @InjectMocks
  CustomProductDependencyRepositoryImpl customProductDependencyRepository;

  @Test
  void testFindProductDependencies() {
    List<ProductDependency> productDependencies = customProductDependencyRepository.findProductDependencies(any(),
        any(), any());
    assertTrue(productDependencies.isEmpty());

    var productDependency = ProductDependency.builder().productId(MOCK_PRODUCT_ID).build();
    when(mongoTemplate.find(any(), any(), any())).thenReturn(List.of(productDependency));
    productDependencies = customProductDependencyRepository.findProductDependencies(any(), any(), any());
    assertFalse(productDependencies.isEmpty());
  }

  @Test
  void testFindMavenArtifactVersions() {
    List<MavenArtifactVersion> mavenArtifactVersions = customProductDependencyRepository.findMavenArtifactVersions(
        any(), any(), any());
    assertTrue(mavenArtifactVersions.isEmpty());

    var mavenArtifactVersion = MavenArtifactVersion.builder().productId(MOCK_PRODUCT_ID).build();
    when(mongoTemplate.find(any(), any(), any())).thenReturn(List.of(mavenArtifactVersion));
    mavenArtifactVersions = customProductDependencyRepository.findMavenArtifactVersions(any(), any(), any());
    assertFalse(mavenArtifactVersions.isEmpty());
  }
}
