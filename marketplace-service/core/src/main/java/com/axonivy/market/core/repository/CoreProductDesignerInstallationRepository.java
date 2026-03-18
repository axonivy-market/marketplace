package com.axonivy.market.core.repository;

import com.axonivy.market.core.entity.ProductDesignerInstallation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoreProductDesignerInstallationRepository extends JpaRepository<ProductDesignerInstallation, String>,
    CoreCustomProductDesignerInstallationRepository {
}
