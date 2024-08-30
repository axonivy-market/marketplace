package com.axonivy.market.repository;

import com.axonivy.market.entity.ProductDesignerInstallation;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDesignerInstallationRepository extends MongoRepository<ProductDesignerInstallation, String>,
        CustomProductRepository {

    List<ProductDesignerInstallation> findByProductId(String productId, Sort sort);
}
