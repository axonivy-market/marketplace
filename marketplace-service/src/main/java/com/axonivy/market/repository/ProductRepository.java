package com.axonivy.market.repository;

import com.axonivy.market.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String>, CustomProductRepository {
  List<Product> findByMarketDirectory(String marketDirectory);
}
