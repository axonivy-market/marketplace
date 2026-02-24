package com.axonivy.market.repository.impl;

import com.axonivy.market.criteria.MonitoringSearchCriteria;
import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.repository.CustomGithubRepoRepository;
import com.axonivy.market.core.repository.CoreAbstractBaseRepository;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.axonivy.market.core.constants.CoreEntityConstants.NAME;
import static com.axonivy.market.constants.PostgresDBConstants.*;
import static com.axonivy.market.core.constants.CorePostgresDBConstants.PRODUCT_ID;

public class CustomGithubRepoRepositoryImpl extends CoreAbstractBaseRepository<GithubRepo>
    implements CustomGithubRepoRepository {
  @Override
  public Page<GithubRepo> findAllByFocusedSorted(MonitoringSearchCriteria criteria, Pageable pageable) {
    String orderBy = getOrderBy(criteria.getWorkFlowType(), criteria.getSortDirection());
    String focusQuery = getFocusQuery(criteria.getIsFocused());

    var productQuery = "";
    if (StringUtils.isNotBlank(criteria.getSearchText())) {
      productQuery = " AND LOWER(r.product_id) LIKE LOWER(CONCAT('%', :productId, '%')) ";
    }

    String querySentence = """
            SELECT r.id, r.name, r.product_id, r.html_url, r.focused
            FROM github_repo r
            LEFT JOIN (
                SELECT w1.repository_id, w1.conclusion
                FROM workflow_information w1
                WHERE w1.workflow_type = :workflowType
                  AND w1.last_built = (
                      SELECT MAX(w2.last_built)
                      FROM workflow_information w2
                      WHERE w2.repository_id = w1.repository_id
                        AND w2.workflow_type = :workflowType
                  )
            ) w ON w.repository_id = r.id
        """ + focusQuery + productQuery + orderBy;

    var nativeQuery = getEntityManager().createNativeQuery(querySentence, GithubRepo.class);
    nativeQuery.setParameter(WORKFLOW_TYPE, criteria.getWorkFlowType());
    String countSql = "SELECT COUNT(*) FROM github_repo r " + focusQuery + productQuery;
    var countQuery = getEntityManager().createNativeQuery(countSql);

    if (StringUtils.isNotBlank(criteria.getSearchText())) {
      nativeQuery.setParameter(PRODUCT_ID, criteria.getSearchText());
      countQuery.setParameter(PRODUCT_ID, criteria.getSearchText());
    }

    nativeQuery.setFirstResult((int) pageable.getOffset());
    nativeQuery.setMaxResults(pageable.getPageSize());

    List<?> resultList = nativeQuery.getResultList();
    List<GithubRepo> githubRepoList = resultList.stream()
        .map(o -> (GithubRepo) o)
        .toList();

    Number total = (Number) countQuery.getSingleResult();

    return new PageImpl<>(githubRepoList, pageable, total.longValue());
  }

  @Override
  protected Class<GithubRepo> getType() {
    return GithubRepo.class;
  }

  private static String getFocusQuery(Boolean isFocused) {
    String focusQuery;
    if (BooleanUtils.isTrue(isFocused)) {
      focusQuery = "WHERE r.focused = true ";
    } else {
      focusQuery = "WHERE r.focused IS NULL ";
    }
    return focusQuery;
  }

  private static String getOrderBy(String workflowType, String sortDirection) {
    String orderBy;
    String direction;

    if (DESCENDING.equalsIgnoreCase(sortDirection)) {
      direction = ASCENDING;
    } else {
      direction = DESCENDING;
    }

    if (workflowType.equalsIgnoreCase(NAME)) {
      orderBy = "ORDER BY r.product_id " + direction;
    } else if (DESCENDING.equalsIgnoreCase(sortDirection)) {
      orderBy = "ORDER BY CASE w.conclusion WHEN 'success' THEN 1 WHEN 'failure' THEN 2 ELSE 3 END";
    } else {
      orderBy = "ORDER BY CASE w.conclusion WHEN 'success' THEN 2 WHEN 'failure' THEN 1 ELSE 3 END";
    }
    return orderBy;
  }
}
