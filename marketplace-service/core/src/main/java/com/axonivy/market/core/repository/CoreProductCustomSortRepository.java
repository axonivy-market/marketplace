package com.axonivy.market.core.repository;

import com.axonivy.market.core.entity.ProductCustomSort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoreProductCustomSortRepository extends JpaRepository<ProductCustomSort, String> {
}
