package com.axonivy.market.repository.impl;

import com.axonivy.market.criteria.ProductSecurityCriteria;
import com.axonivy.market.entity.ProductSecurityInfo;
import com.axonivy.market.enums.ProductSecuritySortOption;
import com.axonivy.market.repository.CustomProductSecurityInfoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

public class CustomProductSecurityInfoRepositoryImpl implements CustomProductSecurityInfoRepository {

  private static final List<String> SEVERITIES = List.of("critical", "high", "medium", "low");

  private final EntityManager entityManager;

  public CustomProductSecurityInfoRepositoryImpl(EntityManager entityManager) {this.entityManager = entityManager;}

  @Override
  public Page<ProductSecurityInfo> searchProductSecurityAndSorting(ProductSecurityCriteria criteria,
      Pageable pageable) {
    String orderBy = getOrderBy(criteria.getSortOption(), criteria.getSortDirection());
    String whereClause = buildWhereClause(criteria.getSearchText());
    String sql = "SELECT * FROM product_security_info psi" + whereClause + " ORDER BY " + orderBy;

    Query query = entityManager.createNativeQuery(sql, ProductSecurityInfo.class);
    // Bind parameter if search text exists
    String searchText = StringUtils.trimToEmpty(criteria.getSearchText());
    if (searchText != null) {
      query.setParameter(1, "%" + searchText + "%");
    }

    List<?> resultList = query
        .setFirstResult((int) pageable.getOffset())
        .setMaxResults(pageable.getPageSize())
        .getResultList();

    List<ProductSecurityInfo> content = resultList.stream()
        .map(ProductSecurityInfo.class::cast)
        .toList();

    String countSql = "SELECT COUNT(*) FROM product_security_info psi" + whereClause;
    Query countQuery = entityManager.createNativeQuery(countSql);

    if (searchText != null) {
      countQuery.setParameter(1, "%" + searchText + "%");
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
      case COMMIT_DATE -> "psi.last_commit_date " + safeDirection;
      default -> "psi.repo_name " + safeDirection;
    };

    if (sortOption == ProductSecuritySortOption.REPO_NAME) {
      return primaryOrder;
    }
    String statusCol = getStatusColumn(sortOption);
    String statusOrder = "";
    if (statusCol != null) {
      statusOrder = ", array_position(ARRAY['ENABLED','NO_PERMISSION','DISABLED'], " + statusCol + ") " + safeDirection;
    }

    String additionalOrder = ", psi.repo_name " + safeDirection;
    return primaryOrder + statusOrder + additionalOrder;
  }

  private String getStatusColumn(ProductSecuritySortOption sortOption) {
    return switch (sortOption) {
      case DEPENDABOT_ALERTS -> "psi.dependabot_status";
      case CODE_SCANNING_ALERTS -> "psi.code_scanning_status";
      case SECRET_SCANNING_ALERTS -> "psi.secret_scanning_status";
      default -> null;
    };
  }

  private String buildJsonSort(String column, String dir) {
    return SEVERITIES.stream()
        .map(sev -> String.format(
            " CAST(COALESCE(CAST(%s AS jsonb) ->> '%s', '0') AS integer) %s",
            column, sev, dir))
        .collect(Collectors.joining(", "));
  }

  private String buildWhereClause(String searchText) {
    if (StringUtils.isBlank(searchText)) {
      return "";
    }
    return " WHERE psi.repo_name ILIKE ?1";
  }

  private String normalizeDirection(String direction) {
    return "ASC".equalsIgnoreCase(direction) ? "ASC" : "DESC";
  }
}
