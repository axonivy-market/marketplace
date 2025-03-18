package com.axonivy.market.repository;

import com.axonivy.market.entity.ProductDesignerInstallation;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDesignerInstallationRepository extends JpaRepository<ProductDesignerInstallation, String>, CustomProductDesignerInstallationRepository {

  List<ProductDesignerInstallation> findByProductId(String productId, Sort sort);
}
