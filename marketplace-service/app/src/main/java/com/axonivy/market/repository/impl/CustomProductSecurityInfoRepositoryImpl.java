package com.axonivy.market.repository.impl;

import com.axonivy.market.entity.ProductSecurityInfo;
import com.axonivy.market.enums.SecurityMonitorSortOption;
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
  public Page<ProductSecurityInfo> searchProductSecurityAndSorting(String searchText,
      SecurityMonitorSortOption sortOption, Pageable pageable) {

    String dir = "DESC";

    String orderBy = switch (sortOption) {
      case DEPENDABOT_ALERTS ->buildJsonSort("psi.dependabot_alerts", dir);
      case CODE_SCANNING_ALERTS ->buildJsonSort("psi.code_scanning_alerts", dir);
      case SECRET_SCANNING_ALERTS ->"psi.secret_scanning_alerts " + dir;
      case REPO_NAME -> "psi.repo_name " + dir;
    };

    String sql = """
        SELECT *
        FROM product_security_info psi
        ORDER BY """ + orderBy;


    List<ProductSecurityInfo> content = entityManager
        .createNativeQuery(sql, ProductSecurityInfo.class)
        .setFirstResult((int) pageable.getOffset())
        .setMaxResults(pageable.getPageSize())
        .getResultList();

    // 🔹 Count query
    String countSql = "SELECT COUNT(*) FROM product_security_info";

    long total = ((Number) entityManager
        .createNativeQuery(countSql)
        .getSingleResult()).longValue();

    return new PageImpl<>(content, pageable, total);
  }

  private String buildJsonSort(String column, String dir) {
    return SEVERITIES.stream()
        .map(sev -> String.format(
            "CAST(COALESCE(%s::jsonb ->> '%s', '0') AS INT) %s",
            column, sev, dir
        ))
        .reduce((a, b) -> a + ", " + b)
        .orElse("");
  }
}
