package com.axonivy.market.repository;

import com.axonivy.market.entity.ProductDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDependencyRepository extends JpaRepository<ProductDependency, String> {

}
