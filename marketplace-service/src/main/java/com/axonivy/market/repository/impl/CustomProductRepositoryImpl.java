package com.axonivy.market.repository.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.criteria.ProductSearchCriteria;
import com.axonivy.market.entity.Artifact;
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
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
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
public class CustomProductRepositoryImpl extends BaseRepository<Product> implements CustomProductRepository {
  final ProductCustomSortRepository productCustomSortRepo;
  final ProductModuleContentRepository contentRepository;

  public CustomProductRepositoryImpl(ProductCustomSortRepository productCustomSortRepo,
      ProductModuleContentRepository contentRepository) {
    this.productCustomSortRepo = productCustomSortRepo;
    this.contentRepository = contentRepository;
  }

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
    CriteriaQueryContext<Product> context = createCriteriaQueryContext();
    context.root().fetch(PRODUCT_NAMES, JoinType.LEFT);
    context.root().fetch(PRODUCT_SHORT_DESCRIPTION, JoinType.LEFT);
    context.root().fetch(PRODUCT_ARTIFACT, JoinType.LEFT);
    context.query().where(context.builder().equal(context.root().get(ID), id));
    try {
      return entityManager.createQuery(context.query()).getSingleResult();
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
    CriteriaQueryContext<Product> criteriaContext = createCriteriaQueryContext();
    PageRequest pageRequest = (PageRequest) pageable;
    Language language = searchCriteria.getLanguage() != null ? searchCriteria.getLanguage() : Language.EN;
    Predicate predicate = buildCriteriaSearch(searchCriteria, criteriaContext.builder(), criteriaContext.root());

    criteriaContext.query().where(predicate);

    if (pageRequest.getSort().isSorted()) {
      sortByOrders(criteriaContext, pageRequest, language.getValue());
    }

    // Create query
    TypedQuery<Product> query = entityManager.createQuery(criteriaContext.query());
    // Apply pagination
    query.setFirstResult((int) pageable.getOffset()); // Starting row
    query.setMaxResults(pageable.getPageSize()); // Number of results
    // Get results
    List<Product> resultList = query.getResultList();
    // Get total count for pagination
    long total = getTotalCount(criteriaContext.builder(), searchCriteria);

    return new PageImpl<>(resultList, pageable, total);
  }

  private void sortByOrders(CriteriaQueryContext<Product> criteriaContext,
      PageRequest pageRequest, String language) {
    List<Order> orders = new ArrayList<>();
    if (pageRequest != null) {
      pageRequest.getSort().stream().findFirst().ifPresent(order -> {
        SortOption sortOption = SortOption.of(order.getProperty());
        switch (sortOption) {
          case ALPHABETICALLY -> orders.add(sortByAlphabet(criteriaContext, language));
          case RECENT -> orders.add(sortByRecent(criteriaContext));
          case POPULARITY -> orders.add(sortByPopularity(criteriaContext));
          default -> orders.addAll(sortByStandard(criteriaContext, language));
        }
      });
    }
    orders.add(sortById(criteriaContext)); // Always sort by ID as a fallback
    criteriaContext.query().orderBy(orders);
  }

  private List<Order> sortByStandard(CriteriaQueryContext<Product> criteriaContext, String language) {
    List<ProductCustomSort> customSorts = productCustomSortRepo.findAll();
    Join<Product, ProductMarketplaceData> marketplaceJoin = criteriaContext.root().join(PRODUCT_MARKETPLACE_DATA,
        JoinType.LEFT);
    List<Order> orders = new ArrayList<>();
    Order order = criteriaContext.builder().desc(
        criteriaContext.builder().coalesce(marketplaceJoin.get(CUSTOM_ORDER), Integer.MIN_VALUE)
    );
    orders.add(order);
    if (ObjectUtils.isNotEmpty(customSorts)) {
      SortOption sortOptionExtension = SortOption.of(customSorts.get(0).getRuleForRemainder());
      switch (sortOptionExtension) {
        case ALPHABETICALLY -> orders.add(sortByAlphabet(criteriaContext, language));
        case RECENT -> orders.add(sortByRecent(criteriaContext));
        default -> orders.add(sortByPopularity(criteriaContext));
      }
    }
    return orders;
  }

  private Order sortByPopularity(CriteriaQueryContext<Product> criteriaContext) {
    Join<Product, ProductMarketplaceData> marketplaceJoin = criteriaContext.root().join(PRODUCT_MARKETPLACE_DATA,
        JoinType.LEFT);
    return criteriaContext.builder().desc(marketplaceJoin.get(INSTALLATION_COUNT));
  }

  private Order sortByAlphabet(CriteriaQueryContext<Product> criteriaContext, String language) {
    MapJoin<Product, String, String> namesJoin = criteriaContext.root().joinMap(PRODUCT_NAMES, JoinType.LEFT);
    Expression<Object> nameValue = criteriaContext.builder().coalesce(
        criteriaContext.builder().selectCase()
            .when(criteriaContext.builder().equal(namesJoin.key(), language), namesJoin.value())
            .otherwise(criteriaContext.builder().literal("")), criteriaContext.builder().literal("")
    );

    // Return sorting order (ascending)
    return criteriaContext.builder().asc(nameValue);
  }

  private Order sortById(CriteriaQueryContext<Product> criteriaContext) {
    return criteriaContext.builder().asc(criteriaContext.root().get(ID));
  }

  private Order sortByRecent(CriteriaQueryContext<Product> criteriaContext) {
    return criteriaContext.builder().desc(
        criteriaContext.builder().coalesce(criteriaContext.root().get(FIRST_PUBLISHED_DATE),
            criteriaContext.builder().literal(Timestamp.valueOf(CommonConstants.DEFAULT_DATE_TIME))));
  }

  private long getTotalCount(CriteriaBuilder cb, ProductSearchCriteria searchCriteria) {
    CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
    Root<Product> countRoot = countQuery.from(Product.class);
    // Rebuild predicate for the count query using the new Root<Product>
    Predicate countPredicate = buildCriteriaSearch(searchCriteria, cb, countRoot);
    countQuery.select(cb.count(countRoot)).where(countPredicate);
    return entityManager.createQuery(countQuery).getSingleResult();
  }

  @Override
  public Product findByCriteria(ProductSearchCriteria criteria) {
    CriteriaQueryContext<Product> criteriaContext = createCriteriaQueryContext();

    Predicate searchCriteria = buildCriteriaSearch(criteria, criteriaContext.builder(), criteriaContext.root());
    criteriaContext.query().where(searchCriteria);

    List<Product> results = findByCriteria(criteriaContext);
    return results.isEmpty() ? null : results.get(0);
  }

  @Override
  public List<Product> findAllProductsHaveDocument() {
    CriteriaQueryContext<Product> criteriaContext = createCriteriaQueryContext();
    Join<Product, Artifact> artifact = criteriaContext.root().join(PRODUCT_ARTIFACT);
    criteriaContext.query().select(criteriaContext.root()).distinct(true).where(
        criteriaContext.builder().isTrue(artifact.get(DOC)));

    return findByCriteria(criteriaContext);
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

  @Override
  protected Class<Product> getType() {
    return Product.class;
  }
}
