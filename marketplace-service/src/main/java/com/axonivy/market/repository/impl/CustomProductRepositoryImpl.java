package com.axonivy.market.repository.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.entity.Artifact;
import com.axonivy.market.criteria.ProductSearchCriteria;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductCustomSort;
import com.axonivy.market.entity.ProductMarketplaceData;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.enums.DocumentField;
import com.axonivy.market.enums.Language;
import com.axonivy.market.enums.SortOption;
import com.axonivy.market.enums.TypeOption;
import com.axonivy.market.repository.CustomProductRepository;
import com.axonivy.market.repository.ProductCustomSortRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.axonivy.market.constants.PostgresDBConstants.*;

@Builder
@AllArgsConstructor
public class CustomProductRepositoryImpl implements CustomProductRepository {
  final ProductCustomSortRepository productCustomSortRepo;
  final ProductModuleContentRepository contentRepository;
  EntityManager em;

  @Override
  public Product getProductByIdAndVersion(String id, String version) {
    Product result = findProductByIdAndRelatedData(id);
    if (!Objects.isNull(result)) {
      ProductModuleContent content = contentRepository.findByVersionAndProductId(version, id);
      if (content != null) {
        Hibernate.initialize(content.getDescription());
        Hibernate.initialize(content.getSetup());
        Hibernate.initialize(content.getDemo());
        result.setProductModuleContent(content);
      }
    }
    return result;
  }

  @Override
  public Product findProductByIdAndRelatedData(String id) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Product> cq = cb.createQuery(Product.class);
    Root<Product> product = cq.from(Product.class);

    product.fetch(PRODUCT_NAMES, JoinType.LEFT);
    product.fetch(PRODUCT_SHORT_DESCRIPTION, JoinType.LEFT);
    product.fetch(PRODUCT_ARTIFACT, JoinType.LEFT);

    cq.where(cb.equal(product.get(ID), id));
    try {
      return em.createQuery(cq).getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public List<String> getReleasedVersionsById(String id) {
    return Optional.ofNullable(findProductByIdAndRelatedData(id))
        .map(Product::getReleasedVersions)
        .orElse(Collections.emptyList());
  }

  @Override
  public Page<Product> searchByCriteria(ProductSearchCriteria searchCriteria, Pageable pageable) {
    ProductCriteriaBuilder<CriteriaBuilder, CriteriaQuery<Product>, Root<Product>> jpaBuilder = createCriteriaQuery();
    PageRequest pageRequest = (PageRequest) pageable;
    Language language = searchCriteria.getLanguage() != null ? searchCriteria.getLanguage() : Language.EN;
    Predicate predicate = buildCriteriaSearch(searchCriteria, jpaBuilder.cb(), jpaBuilder.root());

    jpaBuilder.cq().where(predicate);

    if (pageRequest.getSort().isSorted()) {
      sortByOrders(jpaBuilder,pageRequest, language.getValue());
    }

    // Create query
    TypedQuery<Product> query = em.createQuery(jpaBuilder.cq());
    // Apply pagination
    query.setFirstResult((int) pageable.getOffset()); // Starting row
    query.setMaxResults(pageable.getPageSize()); // Number of results
    // Get results
    List<Product> resultList = query.getResultList();
    // Get total count for pagination
    long total = getTotalCount(jpaBuilder.cb(), searchCriteria);

    return new PageImpl<>(resultList, pageable, total);
  }

  private void sortByOrders(ProductCriteriaBuilder<CriteriaBuilder, CriteriaQuery<Product>, Root<Product>> jpaBuilder,
      PageRequest pageRequest, String language) {
    List<Order> orders = new ArrayList<>();
    if (pageRequest != null) {
      pageRequest.getSort().stream().findFirst().ifPresent(order -> {
        SortOption sortOption = SortOption.of(order.getProperty());
        switch (sortOption) {
          case ALPHABETICALLY -> orders.add(sortByAlphabet(jpaBuilder,language));
          case RECENT -> orders.add(sortByRecent(jpaBuilder));
          case POPULARITY -> orders.add(sortByPopularity(jpaBuilder));
          default -> orders.addAll(sortByStandard(jpaBuilder, language));
        }
      });
    }
    orders.add(sortById(jpaBuilder)); // Always sort by ID as a fallback
    jpaBuilder.cq.orderBy(orders);
  }

  private List<Order> sortByStandard(
      ProductCriteriaBuilder<CriteriaBuilder, CriteriaQuery<Product>, Root<Product>> jpaBuilder, String language) {
    List<ProductCustomSort> customSorts = productCustomSortRepo.findAll();
    Join<Product, ProductMarketplaceData> marketplaceJoin = jpaBuilder.root.join(PRODUCT_MARKETPLACE_DATA,
        JoinType.LEFT);
    List<Order> orders = new ArrayList<>();
    Order order = jpaBuilder.cb.desc(
        jpaBuilder.cb.coalesce(marketplaceJoin.get(CUSTOM_ORDER), Integer.MIN_VALUE)
    );
    orders.add(order);
    if (ObjectUtils.isNotEmpty(customSorts)) {
      SortOption sortOptionExtension = SortOption.of(customSorts.get(0).getRuleForRemainder());
      switch (sortOptionExtension) {
        case ALPHABETICALLY -> orders.add(sortByAlphabet(jpaBuilder, language));
        case RECENT -> orders.add(sortByRecent(jpaBuilder));
        default -> orders.add(sortByPopularity(jpaBuilder));
      }
    }
    return orders;
  }

  private Order sortByPopularity(
      ProductCriteriaBuilder<CriteriaBuilder, CriteriaQuery<Product>, Root<Product>> jpaBuilder) {
    Join<Product, ProductMarketplaceData> marketplaceJoin = jpaBuilder.root.join(PRODUCT_MARKETPLACE_DATA,
        JoinType.LEFT);
    return jpaBuilder.cb.desc(marketplaceJoin.get(INSTALLATION_COUNT));
  }

  private Order sortByAlphabet(
      ProductCriteriaBuilder<CriteriaBuilder, CriteriaQuery<Product>, Root<Product>> jpaBuilder, String language) {
    Subquery<String> subquery = jpaBuilder.cq().subquery(String.class);
    Root<Product> subRoot = subquery.correlate(jpaBuilder.root());
    MapJoin<Product, String, String> subNamesJoin = subRoot.joinMap(PRODUCT_NAMES, JoinType.LEFT);
    subquery.select(subNamesJoin.value())
        .where(jpaBuilder.cb().equal(subNamesJoin.key(), language));

    Expression<String> nameValue = jpaBuilder.cb().coalesce(subquery, jpaBuilder.cb().literal(""));

    // Return sorting order (ascending)
    return jpaBuilder.cb().asc(nameValue);
  }

  private Order sortById(ProductCriteriaBuilder<CriteriaBuilder, CriteriaQuery<Product>, Root<Product>> jpaBuilder){
    return jpaBuilder.cb.asc(jpaBuilder.root().get(ID));
  }

  private Order sortByRecent(
      ProductCriteriaBuilder<CriteriaBuilder, CriteriaQuery<Product>, Root<Product>> jpaBuilder) {
    return jpaBuilder.cb.desc(jpaBuilder.cb.coalesce(jpaBuilder.root().get(FIRST_PUBLISHED_DATE),
            jpaBuilder.cb.literal(Timestamp.valueOf(CommonConstants.DEFAULT_DATE_TIME))));
  }

  private long getTotalCount(CriteriaBuilder cb, ProductSearchCriteria searchCriteria) {
    CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
    Root<Product> countRoot = countQuery.from(Product.class);
    // Rebuild predicate for the count query using the new Root<Product>
    Predicate countPredicate = buildCriteriaSearch(searchCriteria, cb, countRoot);
    countQuery.select(cb.countDistinct(countRoot)).where(countPredicate);
    return em.createQuery(countQuery).getSingleResult();
  }

  @Override
  public Product findByCriteria(ProductSearchCriteria criteria) {
    ProductCriteriaBuilder<CriteriaBuilder, CriteriaQuery<Product>, Root<Product>> jpaBuilder = createCriteriaQuery();

    Predicate searchCriteria = buildCriteriaSearch(criteria, jpaBuilder.cb(), jpaBuilder.root());
    jpaBuilder.cq().where(searchCriteria);

    List<Product> results = em.createQuery(jpaBuilder.cq()).getResultList();

    return results.isEmpty() ? null : results.get(0);
  }

  @Override
  public List<Product> findAllProductsHaveDocument() {
    ProductCriteriaBuilder<CriteriaBuilder, CriteriaQuery<Product>, Root<Product>> jpaBuilder = createCriteriaQuery();
    Join<Product, Artifact> artifact = jpaBuilder.root().join(PRODUCT_ARTIFACT);
    jpaBuilder.cq().select(jpaBuilder.root()).distinct(true).where(jpaBuilder.cb().isTrue(artifact.get(DOC)));
    return em.createQuery(jpaBuilder.cq()).getResultList();
  }

  public Predicate buildCriteriaSearch(ProductSearchCriteria searchCriteria, CriteriaBuilder cb,
      Root<Product> productRoot) {
    List<Predicate> predicates = new ArrayList<>();

    // Query by Listed (Assuming "listed" is a boolean field)
    if (searchCriteria.isListed()) {
      predicates.add(
          cb.or(cb.notEqual(productRoot.get(LISTED), false), cb.isNull(productRoot.get(LISTED)))
      );
    }

    // Query by Type (Assuming "type" is stored as a string or enum code)
    if (searchCriteria.getType() != null && TypeOption.ALL != searchCriteria.getType()) {
      predicates.add(cb.equal(productRoot.get(TYPE), searchCriteria.getType().getCode()));
    }

    // Query by Keyword (Using LIKE for partial matching)
    if (StringUtils.isNotBlank(searchCriteria.getKeyword())) {
      predicates.add(createQueryByKeywordRegex(searchCriteria, cb, productRoot));
    }

    // Combine all conditions using AND
    return cb.and(predicates.toArray(new Predicate[0]));
  }


  private static Predicate createQueryByKeywordRegex(ProductSearchCriteria searchCriteria, CriteriaBuilder cb,
      Root<Product> productRoot) {
    List<Predicate> filters = new ArrayList<>();
    Language language = searchCriteria.getLanguage() != null ? searchCriteria.getLanguage() : Language.EN;
    List<DocumentField> filterProperties = new ArrayList<>(ProductSearchCriteria.DEFAULT_SEARCH_FIELDS);
    if (ObjectUtils.isNotEmpty(searchCriteria.getFields())) {
      filterProperties.clear();
      filterProperties.addAll(searchCriteria.getFields());
    }
    if (ObjectUtils.isNotEmpty(searchCriteria.getExcludeFields())) {
      filterProperties.removeIf(field -> searchCriteria.getExcludeFields().stream()
          .anyMatch(excludeField -> excludeField.name().equals(field.name())));
    }

    for (DocumentField property : filterProperties) {
      if (property.isLocalizedSupport()) {
        String keywordPattern = "%" + searchCriteria.getKeyword().toLowerCase() + "%";
        // Correctly join the Map<String, String> names collection
        MapJoin<Product, String, String> namesJoin = productRoot.joinMap(property.getFieldName());
        // Extract key (language) and value (name)
        Path<String> languageKey = namesJoin.key();
        Path<String> nameValue = namesJoin.value();
        // Filter by language key
        Predicate languageFilter = cb.equal(languageKey, language.name().toLowerCase());
        // Apply keyword search on product names (value)
        Predicate keywordFilter = cb.like(cb.lower(nameValue), keywordPattern);
        // Combine conditions
        filters.add(cb.and(languageFilter, keywordFilter));
      } else {
        filters.add(cb.equal(productRoot.get(property.getFieldName()), searchCriteria.getKeyword()));
      }
    }
    return cb.or(filters.toArray(new Predicate[0])); // Return OR condition for broader match
  }

  private ProductCriteriaBuilder<CriteriaBuilder, CriteriaQuery<Product>, Root<Product>> createCriteriaQuery() {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Product> cq = cb.createQuery(Product.class);
    Root<Product> productRoot = cq.from(Product.class);
    return new ProductCriteriaBuilder<>(cb, cq, productRoot);
  }

  record ProductCriteriaBuilder<T1, T2, T3>(T1 cb, T2 cq, T3 root) {
  }
}
