package com.axonivy.market.repository;

import com.axonivy.market.entity.ProductSecurityInfo;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSecurityInfoRepository extends JpaRepository<ProductSecurityInfo, String>,
    CustomProductSecurityInfoRepository {

  @Transactional
  void deleteByRepoNameNotIn(List<String> repoNames);

}
