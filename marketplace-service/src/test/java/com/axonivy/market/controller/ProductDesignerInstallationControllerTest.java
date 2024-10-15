package com.axonivy.market.controller;

import com.axonivy.market.model.DesignerInstallation;
import com.axonivy.market.service.ProductDesignerInstallationService;
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
class ProductDesignerInstallationControllerTest {
  public static final String DESIGNER_VERSION = "11.4.0";

  @Mock
  ProductDesignerInstallationService productDesignerInstallationService;

  @InjectMocks
  private ProductDesignerInstallationController productDesignerInstallationController;

  @Test
  void testGetProductDesignerInstallationByProductId() {
    List<DesignerInstallation> models = List.of(new DesignerInstallation(DESIGNER_VERSION, 5));
    Mockito.when(productDesignerInstallationService.findByProductId(Mockito.anyString())).thenReturn(models);
    ResponseEntity<List<DesignerInstallation>> result =
        productDesignerInstallationController.getProductDesignerInstallationByProductId(
        "portal");
    Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
    Assertions.assertEquals(1, Objects.requireNonNull(result.getBody()).size());
    Assertions.assertEquals(DESIGNER_VERSION, result.getBody().get(0).getDesignerVersion());
    Assertions.assertEquals(5, result.getBody().get(0).getNumberOfDownloads());
    Assertions.assertEquals(models, result.getBody());
  }
}
