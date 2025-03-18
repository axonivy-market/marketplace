package com.axonivy.market.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class BaseRepository<T> {
  protected abstract Class<T> getType();

  @Autowired
  EntityManager em;

  protected CriteriaQueryContext<T> createCriteriaQueryContext() {
    CriteriaBuilder builder = em.getCriteriaBuilder();
    CriteriaQuery<T> query = builder.createQuery(getType());
    Root<T> root = query.from(getType());
    return new CriteriaQueryContext<>(builder, query, root);
  }

  protected CriteriaUpdateContext<T> createCriteriaUpdateContext() {
    CriteriaBuilder builder = em.getCriteriaBuilder();
    CriteriaUpdate<T> query = builder.createCriteriaUpdate(getType());
    Root<T> root = query.from(getType());
    return new CriteriaUpdateContext<>(builder, query, root);
  }

  protected <R> CriteriaByTypeContext<T, R> createCriteriaTypeContext(Class<R> resultType) {
    CriteriaBuilder builder = em.getCriteriaBuilder();
    CriteriaQuery<R> query = builder.createQuery(resultType);
    Root<T> root = query.from(getType());
    return new CriteriaByTypeContext<>(builder, query, root);
  }

  public record CriteriaQueryContext<T>(CriteriaBuilder builder, CriteriaQuery<T> query, Root<T> root) {
  }

  public record CriteriaUpdateContext<T>(CriteriaBuilder builder, CriteriaUpdate<T> query, Root<T> root) {
  }

  public record CriteriaByTypeContext<T, R>(CriteriaBuilder builder, CriteriaQuery<R> query, Root<T> root) {
  }
}
