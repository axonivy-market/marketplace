package com.axonivy.market.repository.impl;

import com.axonivy.market.constants.EntityConstants;
import com.axonivy.market.constants.MongoDBConstants;
import com.axonivy.market.criteria.ProductSearchCriteria;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.enums.DocumentField;
import com.axonivy.market.enums.Language;
import com.axonivy.market.enums.TypeOption;
import com.axonivy.market.repository.CustomProductRepository;
import com.axonivy.market.repository.CustomRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import com.axonivy.market.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.MapJoin;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonRegularExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.axonivy.market.enums.DocumentField.LISTED;
import static com.axonivy.market.enums.DocumentField.TYPE;

@Builder
@AllArgsConstructor
public class CustomProductRepositoryImpl extends CustomRepository implements CustomProductRepository {
  public static final String CASE_INSENSITIVITY_OPTION = "i";
  public static final String LOCALIZE_SEARCH_PATTERN = "%s.%s";

  final MongoTemplate mongoTemplate;
  final ProductModuleContentRepository contentRepository;
  EntityManager em;


  public Product queryProductByAggregation(Aggregation aggregation) {
    return Optional.of(mongoTemplate.aggregate(aggregation, EntityConstants.PRODUCT, Product.class))
        .map(AggregationResults::getUniqueMappedResult).orElse(null);
  }

  public List<Product> queryProductsByAggregation(Aggregation aggregation) {
    return Optional.of(mongoTemplate.aggregate(aggregation, EntityConstants.PRODUCT, Product.class))
        .map(AggregationResults::getMappedResults).orElse(Collections.emptyList());
  }

  @Override
  public Product getProductByIdAndVersion(String id, String version) {
    Product result = findProductById(id);
    if (!Objects.isNull(result)) {
      ProductModuleContent content = contentRepository.findByVersionAndProductId(version, id);
      result.setProductModuleContent(content);
    }
    return result;
  }

  @Override
  public Product findProductById(String id) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Product> cq = cb.createQuery(Product.class);
    Root<Product> product = cq.from(Product.class);
    cq.where(cb.equal(product.get("id"), id));
    try {
      return em.createQuery(cq).getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  @Override
  public List<String> getReleasedVersionsById(String id) {
    Aggregation aggregation = Aggregation.newAggregation(createIdMatchOperation(id));
    Product product = queryProductByAggregation(aggregation);
    if (Objects.isNull(product)) {
      return Collections.emptyList();
    }
    return product.getReleasedVersions();
  }

  @Override
  public Page<Product> searchByCriteria(ProductSearchCriteria searchCriteria, Pageable pageable) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Product> cq = cb.createQuery(Product.class);
    Root<Product> productRoot = cq.from(Product.class);
    Predicate predicate = buildCriteriaSearch(searchCriteria, cb, productRoot);
    cq.where(predicate);
    // Create query
    TypedQuery<Product> query = em.createQuery(cq);
    // Apply pagination
    query.setFirstResult((int) pageable.getOffset()); // Starting row
    query.setMaxResults(pageable.getPageSize()); // Number of results
    // Get results
    List<Product> resultList = query.getResultList();
    // Get total count for pagination
    long total = getTotalCount(cb, searchCriteria);

    return new PageImpl<>(resultList, pageable, total);
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
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Product> cq = cb.createQuery(Product.class);
    Root<Product> productRoot = cq.from(Product.class);

    Predicate searchCriteria = buildCriteriaSearch(criteria, cb, productRoot);
    cq.where(searchCriteria);

    List<Product> results = em.createQuery(cq).getResultList();

    return results.isEmpty() ? null : results.get(0);
  }

  private CriteriaBuilder createCriteriaBuilder(){
    return em.getCriteriaBuilder();
  }

  @Override
  public List<Product> findAllProductsHaveDocument() {
    var criteria = new Criteria();
    criteria.andOperator(Criteria.where(MongoDBConstants.ARTIFACTS_DOC).is(true));
    return mongoTemplate.find(new Query(criteria), Product.class);
  }

//  private Page<Product> getResultAsPageable(Pageable pageable, Criteria criteria) {
//    int skip = (int) pageable.getOffset();
//    int limit = pageable.getPageSize();
//    Aggregation aggregation = Aggregation.newAggregation(
//        Aggregation.match(criteria),
//        Aggregation.lookup(MongoDBConstants.PRODUCT_MARKETPLACE_COLLECTION, MongoDBConstants.ID, MongoDBConstants.ID,
//            MongoDBConstants.MARKETPLACE_DATA),
//        Aggregation.sort(pageable.getSort()),
//        Aggregation.skip(skip),
//        Aggregation.limit(limit)
//    );
//
//    List<Product> entities = mongoTemplate.aggregate(aggregation, MongoDBConstants.PRODUCT_COLLECTION,
//        Product.class).getMappedResults();
//    long count = mongoTemplate.count(new Query(criteria), Product.class);
//    return new PageImpl<>(entities, pageable, count);
//  }

  public Predicate buildCriteriaSearch(ProductSearchCriteria searchCriteria, CriteriaBuilder cb,
      Root<Product> productRoot) {
    List<Predicate> predicates = new ArrayList<>();

    // Query by Listed (Assuming "listed" is a boolean field)
    if (searchCriteria.isListed()) {
      predicates.add(
          cb.or(cb.notEqual(productRoot.get("listed"), false), cb.isNull(productRoot.get("listed")))
      );
    }

    // Query by Type (Assuming "type" is stored as a string or enum code)
    if (searchCriteria.getType() != null && TypeOption.ALL != searchCriteria.getType()) {
      predicates.add(cb.equal(productRoot.get("type"), searchCriteria.getType().getCode()));
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
        if (StringUtils.isNotBlank(searchCriteria.getKeyword())) {
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
        }
      } else {
        filters.add(cb.equal(productRoot.get(property.getFieldName()), searchCriteria.getKeyword()));
      }
    }
    return cb.or(filters.toArray(new Predicate[0])); // Return OR condition for broader match
  }
}
