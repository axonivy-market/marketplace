package com.axonivy.market.repository;

import com.axonivy.market.core.entity.ProductDesignerInstallation;
import com.axonivy.market.core.repository.CoreProductDesignerInstallationRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Primary
public interface ProductDesignerInstallationRepository extends CoreProductDesignerInstallationRepository,
    CustomProductDesignerInstallationRepository {

  List<ProductDesignerInstallation> findByProductId(String productId, Sort sort);
}
