package com.axonivy.market.repository.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.core.criteria.ProductSearchCriteria;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.enums.DocumentField;
import com.axonivy.market.core.enums.Language;
import com.axonivy.market.repository.ProductCustomSortRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static com.axonivy.market.core.constants.CorePostgresDBConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductSearchRepositoryImplTest extends BaseSetup {

  Page<Product> mockResultReturn;
  ProductSearchCriteria searchCriteria;

  @Mock
  ProductCustomSortRepository productCustomSortRepo;

  @Mock
  ProductModuleContentRepository contentRepository;

  @Mock
  private EntityManager em;

  @InjectMocks
  CustomProductRepositoryImpl productListedRepository;

  @BeforeEach
  void setup() {
    searchCriteria = new ProductSearchCriteria();
    mockResultReturn = createPageProductsMock();
    ReflectionTestUtils.setField(productListedRepository, "entityManager", em);
  }

  @Test
  void testSearchByCriteria() {
    TypedQuery<Product> query = mock(TypedQuery.class);
    TypedQuery<Long> countQuery = mock(TypedQuery.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class);
    CriteriaQuery<Product> criteriaQuery = mock(CriteriaQuery.class);
    CriteriaQuery<Long> criteriaCountQuery = mock(CriteriaQuery.class);
    Root<Product> productRoot = mock(Root.class);
    Root<Product> countRoot = mock(Root.class);

    Path<String> nameValue = mock(Path.class);
    Path<String> keyValue = mock(Path.class);

    MapJoin<Product, String, String> namesJoin = mock(MapJoin.class);
    Mockito.<MapJoin<Product, String, String>>when(productRoot.joinMap(any(), any())).thenReturn(namesJoin);
    when(namesJoin.value()).thenReturn(nameValue);
    when(namesJoin.key()).thenReturn(keyValue);

    Predicate predicate = mock(Predicate.class);
    when(productListedRepository.buildCriteriaSearch(searchCriteria, criteriaQuery, cb, productRoot))
        .thenReturn(predicate);

    // For query products
    when(em.getCriteriaBuilder()).thenReturn(cb);
    when(cb.createQuery(Product.class)).thenReturn(criteriaQuery);
    when(criteriaQuery.from(Product.class)).thenReturn(productRoot);
    when(criteriaQuery.select(productRoot)).thenReturn(criteriaQuery);
    when(criteriaQuery.where(predicate)).thenReturn(criteriaQuery);
    when(criteriaQuery.orderBy(anyList())).thenReturn(criteriaQuery);
    when(em.createQuery(criteriaQuery)).thenReturn(query);
    when(query.getResultList()).thenReturn(mockResultReturn.getContent()); // Mocking a result

    // For counting
    when(cb.createQuery(Long.class)).thenReturn(criteriaCountQuery);
    when(criteriaCountQuery.from(Product.class)).thenReturn(countRoot);
    when(productListedRepository.buildCriteriaSearch(searchCriteria, criteriaCountQuery, cb, countRoot))
        .thenReturn(predicate);
    when(criteriaCountQuery.select(any())).thenReturn(criteriaCountQuery);
    when(criteriaCountQuery.where(predicate)).thenReturn(criteriaCountQuery);
    when(em.createQuery(criteriaCountQuery)).thenReturn(countQuery);
    when(countQuery.getSingleResult()).thenReturn(4L);

    Page<Product> result = productListedRepository.searchByCriteria(searchCriteria, PAGEABLE_ALPHABETICALLY);

    assertFalse(result.isEmpty(), "Result is empty");
    assertTrue(result.isFirst(), "Result is not on the first page");
    assertEquals(2, result.getContent().size(), "Unexpected number of products");
    assertTrue(result.getContent().get(0).getNames().containsValue(SAMPLE_PRODUCT_NAME),
        "Expected product name not found in the result");
    verify(criteriaQuery, never()).distinct(true);
  }

  @Test
  void testSearchByCriteriaOrderByStandard() {
    TypedQuery<Product> query = mock(TypedQuery.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class);
    CriteriaQuery<Product> criteriaQuery = mock(CriteriaQuery.class);
    Root<Product> productRoot = mock(Root.class);

    MapJoin<Product, String, String> namesJoin = mock(MapJoin.class);
    Mockito.<MapJoin<Product, String, String>>when(productRoot.joinMap(any(), any())).thenReturn(namesJoin);

    Predicate predicate = mock(Predicate.class);
    when(productListedRepository.buildCriteriaSearch(searchCriteria, criteriaQuery, cb, productRoot))
        .thenReturn(predicate);


    // For Sort standard
    var mockPath = mock(Path.class);
    var mockOrder = mock(Order.class);
    var mockCoalesce = mock(Expression.class);

    when(productRoot.get(PRODUCT_MARKETPLACE_DATA)).thenReturn(mockPath);
    when(mockPath.get(CUSTOM_ORDER)).thenReturn(mockPath);

    // Mock coalesce expression
    when(cb.coalesce(mockPath, Integer.MIN_VALUE)).thenReturn(mockCoalesce);

    // Mock descending order with coalesce
    when(cb.desc(mockCoalesce)).thenReturn(mockOrder);

    // For query products
    when(em.getCriteriaBuilder()).thenReturn(cb);
    when(cb.createQuery(Product.class)).thenReturn(criteriaQuery);
    when(criteriaQuery.from(Product.class)).thenReturn(productRoot);
    when(criteriaQuery.select(productRoot)).thenReturn(criteriaQuery);
    when(criteriaQuery.where(predicate)).thenReturn(criteriaQuery);
    when(criteriaQuery.orderBy(anyList())).thenReturn(criteriaQuery);
    when(em.createQuery(criteriaQuery)).thenReturn(query);
    when(query.getResultList()).thenReturn(mockResultReturn.getContent()); // Mocking a result

    Page<Product> result = productListedRepository.searchByCriteria(searchCriteria, PAGEABLE_STANDARD);

    assertFalse(result.isEmpty(), "Result is empty");
    assertTrue(result.isFirst(), "Result is not on the first page");
    assertEquals(2, result.getContent().size(), "Unexpected number of products");
    assertTrue(result.getContent().get(0).getNames().containsValue(SAMPLE_PRODUCT_NAME),
        "Expected product name not found in the result");

  }

  @Test
  void testFindAllProductsHaveDocument() {
    TypedQuery<Product> query = mock(TypedQuery.class);
    CriteriaBuilder mockCriteriaBuilder = mock(CriteriaBuilder.class);
    CriteriaQuery<Product> criteriaQuery = mock(CriteriaQuery.class);
    Root<Product> productRoot = mock(Root.class);

    Predicate predicate = mock(Predicate.class);

    when(em.getCriteriaBuilder()).thenReturn(mockCriteriaBuilder);
    when(mockCriteriaBuilder.createQuery(Product.class)).thenReturn(criteriaQuery);
    when(criteriaQuery.from(Product.class)).thenReturn(productRoot);

    var artifactJoin = mock(Join.class);

    when(productRoot.join(PRODUCT_ARTIFACT)).thenReturn(artifactJoin);
    when(mockCriteriaBuilder.isTrue(artifactJoin.get("doc"))).thenReturn(predicate);
    when(criteriaQuery.select(productRoot)).thenReturn(criteriaQuery);
    when(criteriaQuery.distinct(true)).thenReturn(criteriaQuery);
    when(criteriaQuery.where(predicate)).thenReturn(criteriaQuery);

    when(em.createQuery(criteriaQuery)).thenReturn(query);
    when(query.getResultList()).thenReturn(List.of(Product.builder().id("asd").build()));

    List<Product> result = productListedRepository.findAllProductsHaveDocument();

    assertEquals(1, result.size(), "Expected exactly 1 product");
  }


  @Test
  void testFindByCriteria() {
    Product mockProduct = mockResultReturn.getContent().get(0);
    TypedQuery<Product> query = mock(TypedQuery.class);
    CriteriaBuilder mockCriteriaBuilder = mock(CriteriaBuilder.class);
    CriteriaQuery<Product> criteriaQuery = mock(CriteriaQuery.class);
    Root<Product> productRoot = mock(Root.class);

    when(em.getCriteriaBuilder()).thenReturn(mockCriteriaBuilder);
    when(mockCriteriaBuilder.createQuery(Product.class)).thenReturn(criteriaQuery);
    when(criteriaQuery.from(Product.class)).thenReturn(productRoot);

    when(em.createQuery(criteriaQuery)).thenReturn(query);
    when(query.getResultList()).thenReturn(List.of(mockProduct));

    Subquery<Integer> localizedMatch = mock(Subquery.class);
    Root<Product> correlatedProduct = mock(Root.class);
    MapJoin<Product, String, String> localizedJoin = mock(MapJoin.class);
    Path<String> localizedValue = mock(Path.class);
    Expression<String> normalizedValue = mock(Expression.class);
    Expression<Integer> one = mock(Expression.class);
    Predicate keywordPredicate = mock(Predicate.class);

    when(criteriaQuery.subquery(Integer.class)).thenReturn(localizedMatch);
    when(localizedMatch.correlate(productRoot)).thenReturn(correlatedProduct);
    Mockito.<MapJoin<Product, String, String>>when(correlatedProduct.joinMap(any(), any()))
        .thenReturn(localizedJoin);
    when(localizedJoin.value()).thenReturn(localizedValue);
    when(mockCriteriaBuilder.lower(localizedValue)).thenReturn(normalizedValue);
    when(mockCriteriaBuilder.like(any(Expression.class), any(String.class))).thenReturn(keywordPredicate);
    when(mockCriteriaBuilder.literal(1)).thenReturn(one);
    when(localizedMatch.select(one)).thenReturn(localizedMatch);
    when(localizedMatch.where(keywordPredicate)).thenReturn(localizedMatch);


    Product result = productListedRepository.findByCriteria(searchCriteria);

    assertNotNull(result, "Result is empty");
    assertEquals(mockProduct.getId(), result.getId(), "Product ID " + result.getId());

    String productName = mockProduct.getNames().get(Language.EN.getValue());
    searchCriteria.setKeyword(productName);
    result = productListedRepository.findByCriteria(searchCriteria);
    assertNotNull(result, "Result is empty");
    assertEquals(productName, result.getNames().get(Language.EN.getValue()), "Product Name " + result.getNames());

    searchCriteria.setFields(List.of(DocumentField.MARKET_DIRECTORY));
    searchCriteria.setKeyword(mockProduct.getMarketDirectory());
    result = productListedRepository.findByCriteria(searchCriteria);
    assertNotNull(result, "Result is empty");
    assertEquals(mockProduct.getMarketDirectory(), result.getMarketDirectory(),
        "Product MarketDirectory " + result.getMarketDirectory());
  }

  @ParameterizedTest
  @EnumSource(value = DocumentField.class, names = {"NAMES", "SHORT_DESCRIPTIONS"})
  void testKeywordSearchMatchesLocalizedValuesInAnyLanguage(DocumentField field) {
    CriteriaBuilder cb = mock(CriteriaBuilder.class);
    CriteriaQuery<Product> criteriaQuery = mock(CriteriaQuery.class);
    Subquery<Integer> localizedMatch = mock(Subquery.class);
    Root<Product> productRoot = mock(Root.class);
    Root<Product> correlatedProduct = mock(Root.class);
    MapJoin<Product, String, String> localizedJoin = mock(MapJoin.class);
    Path<String> localizedValue = mock(Path.class);
    Expression<String> normalizedValue = mock(Expression.class);
    Predicate keywordPredicate = mock(Predicate.class);
    Predicate existsPredicate = mock(Predicate.class);
    Expression<Integer> one = mock(Expression.class);

    searchCriteria.setKeyword("connector");
    searchCriteria.setLanguage(Language.EN);
    searchCriteria.setFields(List.of(field));
    when(criteriaQuery.subquery(Integer.class)).thenReturn(localizedMatch);
    when(localizedMatch.correlate(productRoot)).thenReturn(correlatedProduct);
    Mockito.<MapJoin<Product, String, String>>when(correlatedProduct.joinMap(field.getFieldName(), JoinType.INNER))
        .thenReturn(localizedJoin);
    when(localizedJoin.value()).thenReturn(localizedValue);
    when(cb.lower(localizedValue)).thenReturn(normalizedValue);
    when(cb.like(normalizedValue, "%connector%")).thenReturn(keywordPredicate);
    when(cb.literal(1)).thenReturn(one);
    when(localizedMatch.select(one)).thenReturn(localizedMatch);
    when(localizedMatch.where(keywordPredicate)).thenReturn(localizedMatch);
    when(cb.exists(localizedMatch)).thenReturn(existsPredicate);

    productListedRepository.buildCriteriaSearch(searchCriteria, criteriaQuery, cb, productRoot);

    verify(cb).exists(localizedMatch);
    verify(localizedJoin, never()).key();
  }

  @Test
  void testGetProductByIdAndVersion() {
    Product mockProduct = mockResultReturn.getContent().get(0);
    TypedQuery<Product> query = mock(TypedQuery.class);
    CriteriaBuilder mockCriteriaBuilder = mock(CriteriaBuilder.class);
    CriteriaQuery<Product> criteriaQuery = mock(CriteriaQuery.class);
    Root<Product> productRoot = mock(Root.class);

    when(em.getCriteriaBuilder()).thenReturn(mockCriteriaBuilder);
    when(mockCriteriaBuilder.createQuery(Product.class)).thenReturn(criteriaQuery);
    when(criteriaQuery.from(Product.class)).thenReturn(productRoot);

    when(em.createQuery(criteriaQuery)).thenReturn(query);
    when(query.getSingleResult()).thenReturn(mockProduct);


    Product result = productListedRepository.getProductByIdAndVersion(mockProduct.getId(), mockProduct.getVersion());

    assertNotNull(result, "Result is empty");
    assertEquals(mockProduct.getId(), result.getId(), "Product ID " + result.getId());
  }

}
