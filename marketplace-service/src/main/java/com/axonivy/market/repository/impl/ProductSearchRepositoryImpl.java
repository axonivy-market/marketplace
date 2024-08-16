package com.axonivy.market.repository.impl;

import static com.axonivy.market.enums.DocumentField.LISTED;
import static com.axonivy.market.enums.DocumentField.TYPE;

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

import com.axonivy.market.criteria.ProductSearchCriteria;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.DocumentField;
import com.axonivy.market.enums.Language;
import com.axonivy.market.enums.TypeOption;
import com.axonivy.market.repository.ProductSearchRepository;

public class ProductSearchRepositoryImpl implements ProductSearchRepository {

  public static final String CASE_INSENSITIVITY_OPTION = "i";
  public static final String LOCALIZE_SEARCH_PATTERN = "%s.%s";

  private final MongoTemplate mongoTemplate;

  public ProductSearchRepositoryImpl(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }


  @Override
  public Page<Product> searchByCriteria(ProductSearchCriteria searchCriteria, Pageable pageable) {
    return getResultAsPageable(pageable, buildCriteriaSearch(searchCriteria));
  }

  @Override
  public Product findByCriteria(ProductSearchCriteria criteria) {
    Criteria searchCriteria = buildCriteriaSearch(criteria);
    List<Product> entities = mongoTemplate.find(new Query(searchCriteria), Product.class);
    return CollectionUtils.isEmpty(entities) ? null : entities.get(0);
  }

  private Page<Product> getResultAsPageable(Pageable pageable, Criteria criteria) {
    var query = new Query(criteria);
    query.with(pageable);
    List<Product> entities = mongoTemplate.find(query, Product.class);
    long count = mongoTemplate.count(new Query(criteria), Product.class);
    return new PageImpl<>(entities, pageable, count);
  }

  private Criteria buildCriteriaSearch(ProductSearchCriteria searchCriteria) {
    var criteria = new Criteria();
    List<Criteria> andFilters = new ArrayList<>();

    // Query by Listed
    if (searchCriteria.isListed()) {
      andFilters.add(Criteria.where(LISTED.getFieldName()).ne(false));
    }

    // Query by Type
    if (searchCriteria.getType() != null && TypeOption.ALL != searchCriteria.getType()) {
      Criteria typeCriteria = Criteria.where(TYPE.getFieldName()).is(searchCriteria.getType().getCode());
      andFilters.add(typeCriteria);
    }

    // Query by Keyword regex
    if (StringUtils.isNoneBlank(searchCriteria.getKeyword())) {
      Criteria keywordCriteria = createQueryByKeywordRegex(searchCriteria);
      if (keywordCriteria != null) {
        andFilters.add(keywordCriteria);
      }
    }

    if (!CollectionUtils.isEmpty(andFilters)) {
      criteria.andOperator(andFilters);
    }
    return criteria;
  }

  private Criteria createQueryByKeywordRegex(ProductSearchCriteria searchCriteria) {
    List<Criteria> filters = new ArrayList<>();
    var language = searchCriteria.getLanguage();
    if (language == null) {
      language = Language.EN;
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
      Criteria filterByKeywordCriteria;
      if (property.isLocalizedSupport()) {
        filterByKeywordCriteria = Criteria.where(LOCALIZE_SEARCH_PATTERN.formatted(property.getFieldName(), language.getValue()));
      } else {
        filterByKeywordCriteria = Criteria.where(property.getFieldName());
      }
      var regex = new BsonRegularExpression(searchCriteria.getKeyword(), CASE_INSENSITIVITY_OPTION);
      filters.add(filterByKeywordCriteria.regex(regex));
    }
    Criteria criteria = null;
    if (!CollectionUtils.isEmpty(filters)) {
      criteria = new Criteria().orOperator(filters);
    }
    return criteria;
  }
}
