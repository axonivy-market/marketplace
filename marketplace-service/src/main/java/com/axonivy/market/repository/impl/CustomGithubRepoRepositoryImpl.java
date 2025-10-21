package com.axonivy.market.repository.impl;

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
import static com.axonivy.market.constants.PostgresDBConstants.PRODUCT_ID;
import static com.axonivy.market.constants.PostgresDBConstants.WORKFLOW_TYPE;

public class CustomGithubRepoRepositoryImpl implements CustomGithubRepoRepository {
  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Page<GithubRepo> findAllByFocusedSorted(Boolean isFocused, String workflowType, String sortDirection,
      String productId, Pageable pageable) {
    String orderBy;

    if (workflowType.equalsIgnoreCase(NAME)) {
      orderBy = "ORDER BY r.product_id " + sortDirection;
    } else if ("DESC".equalsIgnoreCase(sortDirection)) {
      orderBy = "ORDER BY CASE w.conclusion WHEN 'success' THEN 1 WHEN 'failure' THEN 2 ELSE 3 END";
    } else {
      orderBy = "ORDER BY CASE w.conclusion WHEN 'success' THEN 2 WHEN 'failure' THEN 1 ELSE 3 END";
    }

    String focusQuery = BooleanUtils.isTrue(isFocused) ? "WHERE r.focused = true " : "WHERE r.focused IS NULL ";

    String productQuery = "";
    if (StringUtils.isNotBlank(productId)) {
      productQuery = " AND LOWER(r.product_id) LIKE LOWER(CONCAT('%', productId, '%')) ";
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
    nativeQuery.setParameter(WORKFLOW_TYPE, workflowType);
    if (StringUtils.isNotBlank(productId)) {
      nativeQuery.setParameter(PRODUCT_ID, productId);
    }
    nativeQuery.setFirstResult((int) pageable.getOffset());
    nativeQuery.setMaxResults(pageable.getPageSize());

    List<GithubRepo> githubRepoList = nativeQuery.getResultList();
    // Count query
    String countSql = "SELECT COUNT(*) FROM github_repo r " + focusQuery + productQuery;
    Query countQuery = entityManager.createNativeQuery(countSql);
    Number total = (Number) countQuery.getSingleResult();


    return new PageImpl<>(githubRepoList, pageable, total.longValue());
  }
}
