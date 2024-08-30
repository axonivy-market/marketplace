package com.axonivy.market.service;

import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductDesignerInstallation;
import com.axonivy.market.exceptions.model.InvalidParamException;
import com.axonivy.market.model.DesignerInstallation;
import com.axonivy.market.model.ProductCustomSortRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductDesignerInstallationService {
  List<DesignerInstallation> findByProductId(String productId);
}
