package com.axonivy.market.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.service.ProductService;
import com.axonivy.market.service.VersionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;

@ExtendWith(MockitoExtension.class)
class ProductDetailsControllerTest {

  @InjectMocks
  private ProductDetailsController productDetailsController;

  @Mock
  private ProductService productService;

  @Mock
  VersionService versionService;

  @Test
  void testFindProduct() {
    var result = productDetailsController.findProduct("", "");
    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
  }

  @Test
  void testFindProductVersionsById(){
    List<MavenArtifactVersionModel> models = List.of(new MavenArtifactVersionModel());
    when(versionService.getArtifactsAndVersionToDisplay(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString())).thenReturn(models);
    ResponseEntity<List<MavenArtifactVersionModel>> result = productDetailsController.findProductVersionsById("protal", true, "10.0.1");
    Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
    Assertions.assertEquals(1, Objects.requireNonNull(result.getBody()).size());
    Assertions.assertEquals(models, result.getBody());
  }

  @Test
  public void testSyncInstallationCount() throws Exception {
    when(productService.updateInstallationCountForProduct("google-maps-connector")).thenReturn(1);

    var result = productDetailsController.syncInstallationCount("google-maps-connector");

    assertEquals(1, result.getBody());
  }
}