package com.axonivy.market.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public abstract class BaseRepository<T> {
  protected abstract Class<T> getType();

  final EntityManager em;

  protected BaseRepository(EntityManager em) {
    this.em = em;
  }

  protected CriteriaContext<T> createCriteriaContext() {
    CriteriaBuilder builder = em.getCriteriaBuilder();
    CriteriaQuery<T> query = builder.createQuery(getType());
    Root<T> root = query.from(getType());
    return new CriteriaContext<>(builder, query, root);
  }

  public record CriteriaContext<T>(CriteriaBuilder builder, CriteriaQuery<T> query, Root<T> root) {}
}
