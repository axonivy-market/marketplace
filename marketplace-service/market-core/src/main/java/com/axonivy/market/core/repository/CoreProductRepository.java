package com.axonivy.market.core.repository;

import com.axonivy.market.core.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoreProductRepository extends JpaRepository<Product, String> {
}
