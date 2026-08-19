package com.axonivy.market.controller;

import com.axonivy.market.model.DesignerInstallation;
import com.axonivy.market.service.ProductDesignerInstallationService;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerWebMvcTest(ProductDesignerInstallationController.class)
class ProductDesignerInstallationControllerTest extends WebMvcControllerTestSupport {
  public static final String DESIGNER_VERSION = "11.4.0";

  @MockitoBean
  private ProductDesignerInstallationService productDesignerInstallationService;

  @Test
  void testGetProductDesignerInstallationByProductId() throws Exception {
    List<DesignerInstallation> models = List.of(new DesignerInstallation(DESIGNER_VERSION, 5));
    when(productDesignerInstallationService.findByProductId("portal")).thenReturn(models);

    mockMvc.perform(get("/api/product-designer-installation/installation/{id}/designer", "portal"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$..designerVersion").value(hasItem(DESIGNER_VERSION)))
        .andExpect(jsonPath("$..numberOfDownloads").value(hasItem(5)));
  }
}
