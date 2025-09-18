package com.axonivy.market.repository.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.criteria.ProductSearchCriteria;
import com.axonivy.market.entity.Artifact;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductCustomSort;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.enums.DocumentField;
import com.axonivy.market.enums.Language;
import com.axonivy.market.enums.SortOption;
import com.axonivy.market.enums.TypeOption;
import com.axonivy.market.repository.AbstractBaseRepository;
import com.axonivy.market.repository.CustomProductRepository;
import com.axonivy.market.repository.ProductCustomSortRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.Builder;
import lombok.extern.log4j.Log4j2;
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
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import static com.axonivy.market.constants.PostgresDBConstants.*;

import org.springframework.data.domain.Sort;

@Log4j2
@Builder
public class CustomProductRepositoryImpl extends AbstractBaseRepository<Product> implements CustomProductRepository {
  private final ProductCustomSortRepository productCustomSortRepo;
  private final ProductModuleContentRepository contentRepository;

  public CustomProductRepositoryImpl(ProductCustomSortRepository productCustomSortRepo,
      ProductModuleContentRepository contentRepository) {
    this.productCustomSortRepo = productCustomSortRepo;
    this.contentRepository = contentRepository;
  }

  @Override
  public Product getProductByIdAndVersion(String id, String version) {
    var result = findProductByIdAndRelatedData(id);
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
      return getEntityManager().createQuery(context.query()).getSingleResult();
    } catch (NoResultException e) {
      log.error("Cannot find product: ", e);
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
    var pageRequest = (PageRequest) pageable;

    List<Product> resultList = getPagedProductsByCriteria(criteriaContext, searchCriteria, pageRequest);

    long total = resultList.size();
    if (resultList.size() >= pageable.getPageSize()) {
      total = getTotalCount(criteriaContext.builder(), searchCriteria);
    }

    return new PageImpl<>(resultList, pageable, total);
  }

  private List<Order> sortByOrders(
      CriteriaQueryContext<Product> criteriaContext,
      PageRequest pageRequest, String language, MapJoin<Product, String, String> namesJoin) {
    List<Order> orders = new ArrayList<>();
    if (pageRequest != null) {
      pageRequest.getSort().stream().findFirst().ifPresent((Sort.Order order) -> {
        var sortOption = SortOption.of(order.getProperty());
        switch (sortOption) {
          case ALPHABETICALLY -> orders.add(sortByAlphabet(criteriaContext, language, namesJoin));
          case RECENT -> orders.add(sortByRecent(criteriaContext));
          case POPULARITY -> orders.add(sortByPopularity(criteriaContext));
          default -> orders.addAll(sortByStandard(criteriaContext, language, namesJoin));
        }
      });
    }

    // Always sort by ID as a fallback
    orders.add(sortById(criteriaContext));
    return orders;
  }

  private List<Order> sortByStandard(
      CriteriaQueryContext<Product> criteriaContext, String language
      , MapJoin<Product, String, String> namesJoin) {
    List<ProductCustomSort> customSorts = productCustomSortRepo.findAll();
    List<Order> orders = new ArrayList<>();
    var order = criteriaContext.builder().desc(
        criteriaContext.builder().coalesce(criteriaContext.root().get(PRODUCT_MARKETPLACE_DATA).get(CUSTOM_ORDER),
            Integer.MIN_VALUE));
    orders.add(order);
    if (ObjectUtils.isNotEmpty(customSorts)) {
      var sortOptionExtension = SortOption.of(customSorts.get(0).getRuleForRemainder());
      switch (sortOptionExtension) {
        case ALPHABETICALLY -> orders.add(sortByAlphabet(criteriaContext, language, namesJoin));
        case RECENT -> orders.add(sortByRecent(criteriaContext));
        default -> orders.add(sortByPopularity(criteriaContext));
      }
    }
    return orders;
  }

  private static Order sortByPopularity(
      CriteriaQueryContext<Product> criteriaContext) {
    return criteriaContext.builder().desc(criteriaContext.root().get(PRODUCT_MARKETPLACE_DATA).get(INSTALLATION_COUNT));
  }

  private static Order sortByAlphabet(
      CriteriaQueryContext<Product> criteriaContext, String language
      , MapJoin<Product, String, String> namesJoin) {
    Expression<Object> nameValue = criteriaContext.builder().coalesce(
        criteriaContext.builder().selectCase()
            .when(criteriaContext.builder().equal(namesJoin.key(), language), namesJoin.value())
            .otherwise(criteriaContext.builder().literal("")), criteriaContext.builder().literal("")
    );

    // Return sorting order (ascending)
    return criteriaContext.builder().asc(nameValue);
  }

  private static Order sortById(CriteriaQueryContext<Product> criteriaContext) {
    return criteriaContext.builder().asc(criteriaContext.root().get(ID));
  }

  private static Order sortByRecent(CriteriaQueryContext<Product> criteriaContext) {
    return criteriaContext.builder().desc(
        criteriaContext.builder().coalesce(criteriaContext.root().get(FIRST_PUBLISHED_DATE),
            criteriaContext.builder().literal(Timestamp.valueOf(CommonConstants.DEFAULT_DATE_TIME))));
  }

  private long getTotalCount(CriteriaBuilder cb, ProductSearchCriteria searchCriteria) {
    CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
    Root<Product> countRoot = countQuery.from(Product.class);
    // Rebuild predicate for the count query using the new Root<Product>
    var countPredicate = buildCriteriaSearch(searchCriteria, cb, countRoot);
    countQuery.select(cb.countDistinct(countRoot)).where(countPredicate);
    return getEntityManager().createQuery(countQuery).getSingleResult();
  }

  private List<Product> getPagedProductsByCriteria(
      CriteriaQueryContext<Product> criteriaContext,
      ProductSearchCriteria searchCriteria, PageRequest pageRequest) {
    var language = Language.EN;
    if (searchCriteria.getLanguage() != null) {
      language = searchCriteria.getLanguage();
    }

    var predicate = buildCriteriaSearch(searchCriteria, criteriaContext.builder(), criteriaContext.root());
    criteriaContext.root().fetch(PRODUCT_MARKETPLACE_DATA);
    criteriaContext.root().fetch(PRODUCT_NAMES, JoinType.LEFT);
    MapJoin<Product, String, String> namesJoin = criteriaContext.root().joinMap(PRODUCT_NAMES, JoinType.LEFT);

    List<Order> orders = new ArrayList<>();
    if (pageRequest.getSort().isSorted()) {
      orders = sortByOrders(criteriaContext, pageRequest, language.getValue(), namesJoin);
    }

    criteriaContext.query().select(criteriaContext.root()).where(predicate)
        .orderBy(orders)
        .groupBy(criteriaContext.root(), namesJoin.key(), namesJoin.value());

    TypedQuery<Product> query = getEntityManager().createQuery(criteriaContext.query());
    query.setFirstResult((int) pageRequest.getOffset());
    query.setMaxResults(pageRequest.getPageSize());

    return query.getResultList();
  }

  @Override
  public Product findByCriteria(ProductSearchCriteria criteria) {
    CriteriaQueryContext<Product> criteriaContext = createCriteriaQueryContext();

    Predicate searchCriteria = buildCriteriaSearch(criteria, criteriaContext.builder(), criteriaContext.root());
    criteriaContext.query().where(searchCriteria);

    List<Product> results = findByCriteria(criteriaContext);
    if (results.isEmpty()) {
      return null;
    }
    return results.get(0);
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
    var language = Language.EN;
    if (searchCriteria.getLanguage() != null) {
      language = searchCriteria.getLanguage();
    }

    List<DocumentField> filterProperties = new ArrayList<>(ProductSearchCriteria.DEFAULT_SEARCH_FIELDS);
    if (ObjectUtils.isNotEmpty(searchCriteria.getFields())) {
      filterProperties.clear();
      filterProperties.addAll(searchCriteria.getFields());
    }
    if (ObjectUtils.isNotEmpty(searchCriteria.getExcludeFields())) {
      filterProperties.removeIf(field -> searchCriteria.getExcludeFields().stream()
          .anyMatch(excludeField -> excludeField.name().equals(field.name())));
    }

    String keywordPattern = CommonConstants.LIKE_PATTERN.formatted(
        searchCriteria.getKeyword().toLowerCase(Locale.getDefault()));
    for (DocumentField property : filterProperties) {
      if (property.isLocalizedSupport()) {
        // Correctly join the Map<String, String> names collection
        MapJoin<Product, String, String> namesJoin = productRoot.joinMap(property.getFieldName(), JoinType.LEFT);
        // Extract key (language) and value (name)
        Path<String> languageKey = namesJoin.key();
        Path<String> nameValue = namesJoin.value();
        // Filter by language key
        Predicate languageFilter = cb.equal(languageKey, language.name().toLowerCase(Locale.getDefault()));
        // Apply keyword search on product names (value)
        Predicate keywordFilter = cb.like(cb.lower(nameValue), keywordPattern);
        // Combine conditions
        filters.add(cb.and(languageFilter, keywordFilter));
      } else {
        filters.add(cb.equal(productRoot.get(property.getFieldName()), searchCriteria.getKeyword()));
      }
    }

    // Return OR condition for broader match
    return cb.or(filters.toArray(new Predicate[0]));
  }

  @Override
  protected Class<Product> getType() {
    return Product.class;
  }
}
