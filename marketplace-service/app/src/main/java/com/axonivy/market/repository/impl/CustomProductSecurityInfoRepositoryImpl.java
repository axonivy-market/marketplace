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
    String whereClause = buildWhereClause(criteria.getSearchText());

    String sql = "SELECT * FROM product_security_info psi" + whereClause + " ORDER BY " + orderBy;

    var query = entityManager.createNativeQuery(sql, ProductSecurityInfo.class);

    // Bind parameter if search text exists
    if (criteria.getSearchText() != null && !criteria.getSearchText().trim().isEmpty()) {
      query.setParameter(1, "%" + criteria.getSearchText().trim() + "%");
    }

    List<?> resultList = query
        .setFirstResult((int) pageable.getOffset())
        .setMaxResults(pageable.getPageSize())
        .getResultList();
    List<ProductSecurityInfo> content = resultList.stream()
        .map(ProductSecurityInfo.class::cast)
        .toList();

    String countSql = "SELECT COUNT(*) FROM product_security_info psi" + whereClause;
    var countQuery = entityManager.createNativeQuery(countSql);

    if (criteria.getSearchText() != null && !criteria.getSearchText().trim().isEmpty()) {
      countQuery.setParameter(1, "%" + criteria.getSearchText().trim() + "%");
    }

    long total = ((Number) countQuery.getSingleResult()).longValue();

    return new PageImpl<>(content, pageable, total);
  }

  private String getOrderBy(ProductSecuritySortOption sortOption, String direction) {
    String safeDirection = normalizeDirection(direction);
    String primaryOrder = switch (sortOption) {
      case CODE_SCANNING_ALERTS -> buildJsonSort("psi.code_scanning_alerts", safeDirection);
      case DEPENDABOT_ALERTS -> buildJsonSort("psi.dependabot_alerts", safeDirection);
      case SECRET_SCANNING_ALERTS -> "psi.number_of_secret_scanning_alerts " + safeDirection;
      case BRANCH_PROTECTION -> "psi.branch_protection_enabled " + safeDirection;
      case COMMIT_DATE -> "psi.latest_commit_date " + safeDirection;
      default -> "psi.repo_name " + safeDirection;
    };

    // Always add repo_name as deterministic tie-breaker
    if (sortOption == ProductSecuritySortOption.REPO_NAME) {
      return primaryOrder;
    }
    String additionalOrder = ", psi.repo_name " + safeDirection;
    return primaryOrder + additionalOrder;
  }

  private String buildJsonSort(String column, String dir) {
    return SEVERITIES.stream()
        .map(sev -> String.format(" CAST(COALESCE(CAST(%s AS jsonb) ->> '%s', '0') AS integer) %s", column, sev, dir))
        .reduce((a, b) -> a + ", " + b)
        .orElse("");
  }

  private String buildWhereClause(String searchText) {
    if (searchText == null || searchText.trim().isEmpty()) {
      return "";
    }
    return " WHERE psi.repo_name ILIKE ?1";
  }

  private String normalizeDirection(String direction) {
    return "ASC".equalsIgnoreCase(direction) ? "ASC" : "DESC";
  }
}
