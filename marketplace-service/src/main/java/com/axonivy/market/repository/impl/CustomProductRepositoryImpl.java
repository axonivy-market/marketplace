package com.axonivy.market.repository.impl;

import com.axonivy.market.bo.Artifact;
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
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.MapJoin;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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

    Predicate predicate = buildCriteriaSearch(searchCriteria, jpaBuilder.cb(), jpaBuilder.root());

    jpaBuilder.cq().where(predicate);

    if (pageRequest.getSort().isSorted()) {
      sortByOrders(jpaBuilder,pageRequest);
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

  private void sortByOrders(
      ProductCriteriaBuilder<CriteriaBuilder, CriteriaQuery<Product>, Root<Product>> jpaBuilder,
      PageRequest pageRequest) {

    if (pageRequest != null) {
      Sort.Order order = pageRequest.getSort().stream().findFirst().orElse(null);
      SortOption sortOption = SortOption.of(order.getProperty());
      if (SortOption.ALPHABETICALLY == sortOption) {
        sortByAlphabet(jpaBuilder);
      } else if (SortOption.RECENT == sortOption) {
        sortByRecent(jpaBuilder);
      } else if (SortOption.POPULARITY == sortOption) {
        sortByPopularity(jpaBuilder);
      } else if (SortOption.STANDARD == sortOption) {
        sortByStandard(jpaBuilder);
      } else {
        sortByStandard(jpaBuilder);
      }
    }
  }

  private void sortByStandard(
      ProductCriteriaBuilder<CriteriaBuilder, CriteriaQuery<Product>, Root<Product>> jpaBuilder) {
    List<ProductCustomSort> customSorts = productCustomSortRepo.findAll();
    if (!customSorts.isEmpty()) {
      SortOption sortOptionExtension = SortOption.of(customSorts.get(0).getRuleForRemainder());
      if (SortOption.ALPHABETICALLY == sortOptionExtension) {
        sortByAlphabet(jpaBuilder);
      } else if (SortOption.RECENT == sortOptionExtension) {
        sortByRecent(jpaBuilder);
      } else if (SortOption.POPULARITY == sortOptionExtension) {
        sortByPopularity(jpaBuilder);
      }
    }
    Join<Product, ProductMarketplaceData> marketplaceJoin = jpaBuilder.root.join(PRODUCT_MARKETPLACE_DATA,
        JoinType.LEFT);
    Order order = jpaBuilder.cb.asc(marketplaceJoin.get(CUSTOM_ORDER));
    jpaBuilder.cq().orderBy(order);
  }

  private void sortByPopularity(
      ProductCriteriaBuilder<CriteriaBuilder, CriteriaQuery<Product>, Root<Product>> jpaBuilder) {
    Join<Product, ProductMarketplaceData> marketplaceJoin = jpaBuilder.root.join(PRODUCT_MARKETPLACE_DATA,
        JoinType.LEFT);
    Order order = jpaBuilder.cb.desc(marketplaceJoin.get(INSTALLATION_COUNT));
    jpaBuilder.cq().orderBy(order);
  }

  private void sortByAlphabet(
      ProductCriteriaBuilder<CriteriaBuilder, CriteriaQuery<Product>, Root<Product>> jpaBuilder) {
    MapJoin<Product, String, String> namesJoin = jpaBuilder.root().joinMap(PRODUCT_NAMES);
    // Extract key (language) and value (name)
    Path<String> languageKey = namesJoin.key();
    Path<String> nameValue = namesJoin.value();

    Predicate languagePredicate = jpaBuilder.cb.equal(namesJoin.key(), languageKey);
    // Define sorting order
    Order order = jpaBuilder.cb.asc(nameValue);
    jpaBuilder.cq().orderBy(order);
  }

  private void sortByRecent(
      ProductCriteriaBuilder<CriteriaBuilder, CriteriaQuery<Product>, Root<Product>> jpaBuilder) {
    Order order = jpaBuilder.cb.desc(jpaBuilder.root().get(FIRST_PUBLISHED_DATE));
    jpaBuilder.cq().orderBy(order);
  }

  private long getTotalCount(CriteriaBuilder cb, ProductSearchCriteria searchCriteria) {
    CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
    Root<Product> countRoot = countQuery.from(Product.class);
    // Rebuild predicate for the count query using the new Root<Product>
    Predicate countPredicate = buildCriteriaSearch(searchCriteria, cb, countRoot);
    countQuery.select(cb.count(countRoot)).where(countPredicate);
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
