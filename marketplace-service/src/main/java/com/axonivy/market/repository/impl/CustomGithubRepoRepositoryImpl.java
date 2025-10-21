package com.axonivy.market.repository.impl;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.repository.CustomGithubRepoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class CustomGithubRepoRepositoryImpl implements CustomGithubRepoRepository {
  @PersistenceContext
  private EntityManager entityManager;

  @Override
  public Page<GithubRepo> findAllByFocusedSorted(Boolean isFocused, String workflowType, String sortDirection,
      Pageable pageable) {
    String orderBy;
    if ("DESC".equalsIgnoreCase(sortDirection)) {
      orderBy = "ORDER BY CASE w.conclusion WHEN 'success' THEN 2 WHEN 'failure' THEN 1 ELSE 3 END, r.id";
    } else {
      orderBy = "ORDER BY CASE w.conclusion WHEN 'success' THEN 1 WHEN 'failure' THEN 2 ELSE 3 END, r.id";
    }

    String focusQuery = BooleanUtils.isTrue(isFocused) ? "WHERE r.focused = true" : "WHERE r.focused IS NULL ";
    String sql = "SELECT r.* FROM github_repo r " +
        "LEFT JOIN ( " +
        "    SELECT w1.* " +
        "    FROM workflow_information w1 " +
        "    WHERE w1.workflow_type = :workflowType " +
        "    AND w1.last_built = ( " +
        "        SELECT MAX(w2.last_built) " +
        "        FROM workflow_information w2 " +
        "        WHERE w2.repository_id = w1.repository_id AND w2.workflow_type = w1.workflow_type " +
        "    ) " +
        ") w ON w.repository_id = r.id " +
        focusQuery +
        orderBy;

    Query nativeQuery = entityManager.createNativeQuery(sql, GithubRepo.class);
    nativeQuery.setParameter("workflowType", workflowType);
    if (BooleanUtils.isTrue(isFocused)) {
      nativeQuery.setParameter("isFocused", isFocused);
    }
    nativeQuery.setFirstResult((int) pageable.getOffset());
    nativeQuery.setMaxResults(pageable.getPageSize());

    List<GithubRepo> repos = nativeQuery.getResultList();

    // Count query
    String countSql = "SELECT COUNT(*) FROM github_repo r " + focusQuery;
    Query countQuery = entityManager.createNativeQuery(countSql);
    Number total = (Number) countQuery.getSingleResult();

    return new PageImpl<>(repos, pageable, total.longValue());
  }
}
