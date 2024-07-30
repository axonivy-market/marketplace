package com.axonivy.market.repository.impl;

import static com.axonivy.market.repository.enums.DocumentField.LISTED;
import static com.axonivy.market.repository.enums.DocumentField.TYPE;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.BsonRegularExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.CollectionUtils;

import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.Language;
import com.axonivy.market.enums.TypeOption;
import com.axonivy.market.repository.ProductListedRepository;
import com.axonivy.market.repository.criteria.ProductSearchCriteria;
import com.axonivy.market.repository.enums.DocumentField;

public class ProductListedRepositoryImpl implements ProductListedRepository {

  public static final String CASE_INSENSITIVITY_OPTION = "i";
  public static final String LOCALIZE_SEARCH_PATTERN = "%s.%s";

  private final MongoTemplate mongoTemplate;

  public ProductListedRepositoryImpl(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  public Page<Product> findAllListed(Pageable pageable) {
    var criteria = new Criteria();
    criteria.andOperator(createListedFilter().ne(false));
    return findByCriteria(pageable, criteria);
  }

  @Override
  public Page<Product> searchListedByCriteria(ProductSearchCriteria searchCriteria, Pageable pageable) {
    return findByCriteria(pageable, buildCriteriaSearch(searchCriteria, true, false));
  }

  @Override
  public Product findListedByCriteria(ProductSearchCriteria criteria) {
    Criteria searchCriteria = buildCriteriaSearch(criteria, true, false);
    List<Product> entities = mongoTemplate.find(new Query(searchCriteria), Product.class);
    return CollectionUtils.isEmpty(entities) ? null : entities.get(0);
  }

  @Override
  public Product findByCriteria(ProductSearchCriteria criteria) {
    Criteria searchCriteria = buildCriteriaSearch(criteria, false, false);
    List<Product> entities = mongoTemplate.find(new Query(searchCriteria), Product.class);
    return CollectionUtils.isEmpty(entities) ? null : entities.get(0);
  }

  private Page<Product> findByCriteria(Pageable pageable, Criteria criteria) {
    var query = new Query(criteria);
    query.with(pageable);
    List<Product> entities = mongoTemplate.find(query, Product.class);
    long count = mongoTemplate.count(new Query(criteria), Product.class);
    return new PageImpl<>(entities, pageable, count);
  }

  private Criteria createListedFilter() {
    return Criteria.where(LISTED.getFieldName());
  }

  private Criteria buildCriteriaSearch(ProductSearchCriteria searchCriteria, boolean isListed,
      boolean isWrappedQueryByOr) {
    var criteria = new Criteria();
    List<Criteria> andFilters = new ArrayList<>();
    List<Criteria> orFilters = new ArrayList<>();

    // Query by Listed
    if (isListed) {
      Criteria listedCriteria = createListedFilter();
      listedCriteria.ne(false);
      andFilters.add(listedCriteria);
    }

    // Query by Type
    if (searchCriteria.getType() != null && TypeOption.ALL != searchCriteria.getType()) {
      Criteria typeCriteria = Criteria.where(TYPE.getFieldName());
      andFilters.add(typeCriteria.is(searchCriteria.getType().getCode()));
    }

    // Query by Keyword regex
    if (StringUtils.isNoneBlank(searchCriteria.getKeyword())) {
      createQueryByKeywordRegex(searchCriteria, orFilters);
    }

    // Add OR operator is root condition
    if (isWrappedQueryByOr && !CollectionUtils.isEmpty(orFilters)) {
      criteria.orOperator(orFilters);
      if (!CollectionUtils.isEmpty(andFilters)) {
        criteria.andOperator(andFilters);
      }
    } else {
      // Add AND operator is root condition
      if (!CollectionUtils.isEmpty(andFilters)) {
        criteria.andOperator(andFilters);
      }
      if (!CollectionUtils.isEmpty(orFilters)) {
        criteria.orOperator(orFilters);
      }
    }
    return criteria;
  }

  private void createQueryByKeywordRegex(ProductSearchCriteria searchCriteria, List<Criteria> filters) {
    String language = searchCriteria.getLanguage();
    if (StringUtils.isBlank(language)) {
      language = Language.EN.getValue();
    }

    List<DocumentField> filterProperties = new ArrayList<>(ProductSearchCriteria.DEFAULT_SEARCH_FIELDS);
    if (!CollectionUtils.isEmpty(searchCriteria.getFields())) {
      filterProperties.clear();
      filterProperties.addAll(searchCriteria.getFields());
    }
    if (!CollectionUtils.isEmpty(searchCriteria.getExcludeFields())) {
      filterProperties.removeIf(field -> searchCriteria.getExcludeFields().stream()
          .anyMatch(excludeField -> excludeField.name().equals(field.name())));
    }

    for (var property : filterProperties) {
      Criteria filterByKeyworkCriteria = null;
      if (property.isSupportedLocalized()) {
        filterByKeyworkCriteria = Criteria.where(LOCALIZE_SEARCH_PATTERN.formatted(property.getFieldName(), language));
      } else {
        filterByKeyworkCriteria = Criteria.where(property.getFieldName());
      }
      var regex = new BsonRegularExpression(searchCriteria.getKeyword(), CASE_INSENSITIVITY_OPTION);
      filters.add(filterByKeyworkCriteria.regex(regex));
    }
  }

}
