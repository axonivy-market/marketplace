package com.axonivy.market.repository.impl;

import com.axonivy.market.constants.EntityConstants;
import com.axonivy.market.constants.MongoDBConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.criteria.ProductSearchCriteria;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductDesignerInstallation;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.enums.DocumentField;
import com.axonivy.market.enums.Language;
import com.axonivy.market.enums.TypeOption;
import com.axonivy.market.repository.CustomProductRepository;
import com.axonivy.market.repository.CustomRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import com.axonivy.market.util.VersionUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonRegularExpression;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
  final ProductJsonContentRepository jsonContentRepository;

  public Product queryProductByAggregation(Aggregation aggregation) {
    return Optional.of(mongoTemplate.aggregate(aggregation, EntityConstants.PRODUCT, Product.class))
        .map(AggregationResults::getUniqueMappedResult).orElse(null);
  }

  public List<Product> queryProductsByAggregation(Aggregation aggregation) {
    return Optional.of(mongoTemplate.aggregate(aggregation, EntityConstants.PRODUCT, Product.class))
        .map(AggregationResults::getMappedResults).orElse(Collections.emptyList());
  }

  @Override
  public Product getProductByIdAndTag(String id, String tag) {
    Product result = findProductById(id);
    if (!Objects.isNull(result)) {
      ProductModuleContent content = contentRepository.findByTagAndProductId(tag, id);
      result.setProductModuleContent(content);
    }
    return result;
  }

  @Override
  public Product getProductByIdWithNewestReleaseVersion(String id, Boolean isShowDevVersion) {
    Product result = findProductById(id);
    if (ObjectUtils.isEmpty(result)) {
      return null;
    }
    
    //TODO: Check
    List<String> devVersions = VersionUtils.getVersionsToDisplay(result.getReleasedVersions(), isShowDevVersion, null);
    ProductModuleContent content = contentRepository.findByTagAndProductId(devVersions.get(0), id);
    jsonContentRepository.findByProductIdAndVersion(id, devVersions.get(0)).stream().map(
        ProductJsonContent::getContent).findFirst().ifPresent(
        jsonContent -> result.setMavenDropins(isJsonContentContainOnlyMavenDropins(jsonContent)));
    result.setProductModuleContent(content);
    return result;
  }

  private boolean isJsonContentContainOnlyMavenDropins(String jsonContent) {
    return jsonContent.contains(ProductJsonConstants.MAVEN_DROPINS_INSTALLER_ID) && !jsonContent.contains(
        ProductJsonConstants.MAVEN_IMPORT_INSTALLER_ID) && !jsonContent.contains(
        ProductJsonConstants.MAVEN_DEPENDENCY_INSTALLER_ID);
  }

  private Product findProductById(String id) {
    Aggregation aggregation = Aggregation.newAggregation(createIdMatchOperation(id));
    return queryProductByAggregation(aggregation);
  }

  @Override
  public Product getProductById(String id) {
    Product result = findProductById(id);
    if (!Objects.isNull(result)) {
      ProductModuleContent content = contentRepository.findByTagAndProductId(
          result.getNewestReleaseVersion(), id);
      result.setProductModuleContent(content);
    }
    return result;
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

  public int updateInitialCount(String productId, int initialCount) {
    Update update = new Update().inc(MongoDBConstants.INSTALLATION_COUNT, initialCount).set(
        MongoDBConstants.SYNCHRONIZED_INSTALLATION_COUNT, true);
    mongoTemplate.updateFirst(createQueryById(productId), update, Product.class);
    return Optional.ofNullable(getProductById(productId)).map(Product::getInstallationCount).orElse(0);
  }

  @Override
  public int increaseInstallationCount(String productId) {
    Update update = new Update().inc(MongoDBConstants.INSTALLATION_COUNT, 1);
    Product updatedProduct = mongoTemplate.findAndModify(createQueryById(productId), update,
        FindAndModifyOptions.options().returnNew(true), Product.class);
    return updatedProduct != null ? updatedProduct.getInstallationCount() : 0;
  }


  @Override
  public void increaseInstallationCountForProductByDesignerVersion(String productId, String designerVersion) {
    Update update = new Update().inc(MongoDBConstants.INSTALLATION_COUNT, 1);
    mongoTemplate.upsert(createQueryByProductIdAndDesignerVersion(productId, designerVersion),
        update, ProductDesignerInstallation.class);
  }

  @Override
  public List<Product> getAllProductsWithIdAndReleaseTagAndArtifact() {
    return queryProductsByAggregation(
        createProjectIdAndReleasedVersionsAndArtifactsAggregation());
  }

  private Query createQueryByProductIdAndDesignerVersion(String productId, String designerVersion) {
    return new Query(Criteria.where(MongoDBConstants.PRODUCT_ID).is(productId)
        .andOperator(Criteria.where(MongoDBConstants.DESIGNER_VERSION).is(designerVersion)));
  }

  protected Aggregation createProjectIdAndReleasedVersionsAndArtifactsAggregation() {
    return Aggregation.newAggregation(
        Aggregation.project(MongoDBConstants.ID, MongoDBConstants.ARTIFACTS, MongoDBConstants.RELEASED_VERSIONS)
    );
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

  @Override
  public List<Product> findAllProductsHaveDocument() {
    var criteria = new Criteria();
    criteria.andOperator(Criteria.where(MongoDBConstants.ARTIFACTS_DOC).is(true));
    return mongoTemplate.find(new Query(criteria), Product.class);
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
        filterByKeywordCriteria = Criteria.where(
            LOCALIZE_SEARCH_PATTERN.formatted(property.getFieldName(), language.getValue()));
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
