package com.axonivy.market.service.impl;

import com.axonivy.market.constants.MongoDBConstants;
import com.axonivy.market.entity.ProductDesignerInstallation;
import com.axonivy.market.model.DesignerInstallation;
import com.axonivy.market.repository.ProductDesignerInstallationRepository;
import com.axonivy.market.service.ProductDesignerInstallationService;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Log4j2
@Service
public class ProductDesignerInstallationServiceImpl implements ProductDesignerInstallationService {
  private final ProductDesignerInstallationRepository productDesignerInstallationRepository;

  public ProductDesignerInstallationServiceImpl(
      ProductDesignerInstallationRepository productDesignerInstallationRepository) {
    this.productDesignerInstallationRepository = productDesignerInstallationRepository;
  }

  @Override
  public List<DesignerInstallation> findByProductId(String productId) {
    List<DesignerInstallation> designerInstallations = new ArrayList<>();
    List<ProductDesignerInstallation> productDesignerInstallations =
        productDesignerInstallationRepository.findByProductId(productId,
            Sort.by(Sort.Direction.DESC, MongoDBConstants.DESIGNER_VERSION));
    for (ProductDesignerInstallation productDesignerInstallation : productDesignerInstallations) {
      DesignerInstallation designerInstallation = new DesignerInstallation(
          productDesignerInstallation.getDesignerVersion(), productDesignerInstallation.getInstallationCount());
      designerInstallations.add(designerInstallation);
    }
    return designerInstallations;
  }
}
