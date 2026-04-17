package com.axonivy.market.repository.impl;

import com.axonivy.market.criteria.ProductSecurityCriteria;
import com.axonivy.market.entity.ProductSecurityInfo;
import com.axonivy.market.enums.ProductSecuritySortOption;
import com.axonivy.market.repository.CustomProductSecurityInfoRepository;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class CustomProductSecurityInfoRepositoryImpl implements CustomProductSecurityInfoRepository {
  private final EntityManager entityManager;
  private static final List<String> SEVERITIES = List.of("critical", "high", "medium", "low");

  public CustomProductSecurityInfoRepositoryImpl(EntityManager entityManager) {this.entityManager = entityManager;}

  @Override
  public Page<ProductSecurityInfo> searchProductSecurityAndSorting(ProductSecurityCriteria criteria,
      Pageable pageable) {
    String orderBy = getOrderBy(criteria.getSortOption(), criteria.getSortDirection());

    String sql = "SELECT * FROM product_security_info psi ORDER BY " + orderBy;


    List<?> resultList = entityManager
        .createNativeQuery(sql, ProductSecurityInfo.class)
        .setFirstResult((int) pageable.getOffset())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
    List<ProductSecurityInfo> content = resultList.stream()
        .map(ProductSecurityInfo.class::cast)
        .toList();

    String countSql = "SELECT COUNT(*) FROM product_security_info";

    long total = ((Number) entityManager
        .createNativeQuery(countSql)
        .getSingleResult()).longValue();

    return new PageImpl<>(content, pageable, total);
  }

  private String getOrderBy(ProductSecuritySortOption sortOption, String direction) {
    return switch (sortOption) {
      case DEPENDABOT_ALERTS -> buildJsonSort("psi.dependabot_alerts", direction);
      case CODE_SCANNING_ALERTS -> buildJsonSort("psi.code_scanning_alerts", direction);
      case SECRET_SCANNING_ALERTS -> "psi.number_of_secret_scanning_alerts " + direction;
      case REPO_NAME -> "psi.repo_name " + direction;
    };
  }

  private String buildJsonSort(String column, String dir) {
    return SEVERITIES.stream()
        .map(sev -> String.format(" CAST(COALESCE(CAST(%s AS jsonb) ->> '%s', '0') AS integer) %s", column, sev, dir))
        .reduce((a, b) -> a + ", " + b)
        .orElse("");
  }
}
