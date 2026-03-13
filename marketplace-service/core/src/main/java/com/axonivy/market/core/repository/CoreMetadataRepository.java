package com.axonivy.market.core.repository;

import com.axonivy.market.core.entity.Metadata;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoreMetadataRepository extends JpaRepository<Metadata, String> {
  @EntityGraph(attributePaths = {"versions"})
  List<Metadata> findByProductId(String productId);
}
