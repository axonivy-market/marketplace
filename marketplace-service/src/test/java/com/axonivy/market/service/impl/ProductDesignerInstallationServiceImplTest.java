package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.ProductDesignerInstallation;
import com.axonivy.market.model.DesignerInstallation;
import com.axonivy.market.repository.ProductDesignerInstallationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductDesignerInstallationServiceImplTest extends BaseSetup {
  private List<ProductDesignerInstallation> mockResultReturn;
  @Mock
  private ProductDesignerInstallationRepository productDesignerInstallationRepository;

  @InjectMocks
  private ProductDesignerInstallationServiceImpl productDesignerInstallationServiceImpl;

  @BeforeEach
  public void setup() {
    mockResultReturn = createProductDesignerInstallationsMock();
  }

  @Test
  void testFindByProductId() {
    when(productDesignerInstallationRepository.findByProductId(any(), any())).thenReturn(this.mockResultReturn);
    List<DesignerInstallation> results = productDesignerInstallationServiceImpl.findByProductId(BaseSetup.SAMPLE_PRODUCT_ID);
    assertEquals(2,results.size());
    assertEquals("10.0.22", results.get(0).getDesignerVersion());
    assertEquals(50, results.get(0).getNumberOfDownloads());
    assertEquals("11.4.0", results.get(1).getDesignerVersion());
    assertEquals(30, results.get(1).getNumberOfDownloads());
  }
}
