package com.axonivy.market.repository.impl;

import static com.axonivy.market.repository.constants.FieldConstants.LISTED_FIELD;
import static com.axonivy.market.repository.constants.FieldConstants.TYPE_FIELD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.bson.BsonRegularExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.TypeOption;
import com.axonivy.market.repository.ProductListedRepository;
import com.axonivy.market.repository.criteria.ProductSearchCriteria;

public class ProductListedRepositoryImpl implements ProductListedRepository {

  public static final String CASE_INSENSITIVITY_OPTION = "i";
  public static final String SEARCH_BY_FIELD_PATTERN = "%s.%s";

  @Autowired
  private MongoTemplate mongoTemplate;

  @Override
  public Page<Product> findAllListed(Pageable pageable) {
    var criteria = new Criteria();
    criteria.andOperator(buildListedFilter().ne(false));
    return findByCriteria(pageable, criteria);
  }

  private Page<Product> findByCriteria(Pageable pageable, Criteria criteria) {
    var query = new Query(criteria);
    query.with(pageable);
    List<Product> entities = mongoTemplate.find(query, Product.class);
    long count = mongoTemplate.count(new Query(criteria), Product.class);
    return new PageImpl<>(entities, pageable, count);
  }

  private Criteria buildListedFilter() {
    return Criteria.where(LISTED_FIELD);
  }

  @Override
  public Page<Product> searchListedByCriteria(ProductSearchCriteria searchCriteria, Pageable pageable) {
    var criteria = new Criteria();
    List<Criteria> andFilters = new ArrayList<>();
    List<Criteria> orFilters = new ArrayList<>();
    andFilters.add(buildListedFilter().ne(false));
    // Query by Type
    if (searchCriteria.getType() != null && TypeOption.ALL != searchCriteria.getType()) {
      var typeCriteria = Criteria.where(TYPE_FIELD);
      andFilters.add(typeCriteria.is(searchCriteria.getType().getCode()));
    }

    // Query by Keyword
    if (StringUtils.isNoneBlank(searchCriteria.getKeyword())) {
      var locale = searchCriteria.getLanguage();
      if (StringUtils.isBlank(locale)) {
        locale = Locale.ENGLISH.toLanguageTag();
      }
      List<String> filterProperties = Arrays.asList(ProductSearchCriteria.DEFAULT_SEARCH_FIELDS);
      if (searchCriteria.getExcludeProperties() != null && searchCriteria.getExcludeProperties().length > 0) {
        filterProperties.removeAll(Arrays.asList(searchCriteria.getExcludeProperties()));
      }
      for (var property : filterProperties) {
        var filterByKeyworkCriteria = Criteria.where(SEARCH_BY_FIELD_PATTERN.formatted(property, locale));
        BsonRegularExpression regex = new BsonRegularExpression(searchCriteria.getKeyword(), CASE_INSENSITIVITY_OPTION);
        orFilters.add(filterByKeyworkCriteria.regex(regex));
      }
    }
    criteria.andOperator(andFilters);
    criteria.orOperator(orFilters);

    return findByCriteria(pageable, criteria);
  }

}
