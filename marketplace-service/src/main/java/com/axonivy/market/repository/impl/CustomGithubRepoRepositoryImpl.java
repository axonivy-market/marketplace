package com.axonivy.market.repository.impl;

import com.axonivy.market.criteria.MonitoringSearchCriteria;
import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.repository.CustomGithubRepoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.axonivy.market.constants.EntityConstants.NAME;
import static com.axonivy.market.constants.PostgresDBConstants.*;

public class CustomGithubRepoRepositoryImpl implements CustomGithubRepoRepository {

  @PersistenceContext
  private EntityManager entityManager;

  public CustomGithubRepoRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public Page<GithubRepo> findAllByFocusedSorted(MonitoringSearchCriteria criteria) {
    String orderBy = getOrderBy(criteria.getWorkFlowType(), criteria.getSortDirection());
    String focusQuery = getFocusQuery(criteria.getIsFocused());

    String productQuery = "";
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

    Query nativeQuery = entityManager.createNativeQuery(querySentence, GithubRepo.class);
    nativeQuery.setParameter(WORKFLOW_TYPE, criteria.getWorkFlowType());
    String countSql = "SELECT COUNT(*) FROM github_repo r " + focusQuery + productQuery;
    Query countQuery = entityManager.createNativeQuery(countSql);

    if (StringUtils.isNotBlank(criteria.getSearchText())) {
      nativeQuery.setParameter(PRODUCT_ID, criteria.getSearchText());
      countQuery.setParameter(PRODUCT_ID, criteria.getSearchText());
    }

    nativeQuery.setFirstResult((int) criteria.getPageable().getOffset());
    nativeQuery.setMaxResults(criteria.getPageable().getPageSize());

    List<?> resultList = nativeQuery.getResultList();
    List<GithubRepo> githubRepoList = resultList.stream()
        .map(o -> (GithubRepo) o)
        .toList();

    Number total = (Number) countQuery.getSingleResult();

    return new PageImpl<>(githubRepoList, criteria.getPageable(), total.longValue());
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
