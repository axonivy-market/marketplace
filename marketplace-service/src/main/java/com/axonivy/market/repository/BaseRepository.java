package com.axonivy.market.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Component
public abstract class BaseRepository<T> {

  protected abstract Class<T> getType();

  private EntityManager entityManager;

  @Autowired
  public void setEntityManager(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  protected CriteriaQueryContext<T> createCriteriaQueryContext() {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<T> query = builder.createQuery(getType());
    Root<T> root = query.from(getType());
    return new CriteriaQueryContext<>(builder, query, root);
  }

  protected CriteriaUpdateContext<T> createCriteriaUpdateContext() {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaUpdate<T> query = builder.createCriteriaUpdate(getType());
    Root<T> root = query.from(getType());
    return new CriteriaUpdateContext<>(builder, query, root);
  }

  protected <R> CriteriaByTypeContext<T, R> createCriteriaTypeContext(Class<R> resultType) {
    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
    CriteriaQuery<R> query = builder.createQuery(resultType);
    Root<T> root = query.from(getType());
    return new CriteriaByTypeContext<>(builder, query, root);
  }

  protected void save(T entity) {
    entityManager.persist(entity);
  }

  public List<T> findByCriteria(CriteriaQueryContext<T> criteriaQueryContext) {
    return entityManager.createQuery(criteriaQueryContext.query()).getResultList();
  }

  public int executeQuery(CriteriaUpdateContext<T> criteriaUpdateContext) {
    return entityManager.createQuery(criteriaUpdateContext.query()).executeUpdate();
  }

  public <R> List<R> findByCriteria(CriteriaByTypeContext<T, R> criteriaByTypeContext) {
    return entityManager.createQuery(criteriaByTypeContext.query()).getResultList();
  }

  public record CriteriaQueryContext<T>(CriteriaBuilder builder, CriteriaQuery<T> query, Root<T> root) {
  }

  public record CriteriaUpdateContext<T>(CriteriaBuilder builder, CriteriaUpdate<T> query, Root<T> root) {
  }

  public record CriteriaByTypeContext<T, R>(CriteriaBuilder builder, CriteriaQuery<R> query, Root<T> root) {
  }
}
