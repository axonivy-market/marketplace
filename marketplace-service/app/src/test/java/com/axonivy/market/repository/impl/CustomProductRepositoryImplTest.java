package com.axonivy.market.repository.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static com.axonivy.market.core.constants.CorePostgresDBConstants.ID;
import static com.axonivy.market.core.constants.CorePostgresDBConstants.LISTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomProductRepositoryImplTest extends BaseSetup {
  @Mock
  ProductModuleContentRepository contentRepo;
  private Product mockProduct;
  @Mock
  private MetadataRepository metadataRepo;

  @InjectMocks
  private CustomProductRepositoryImpl repo;

  @Mock
  private EntityManager em;

  @BeforeEach
  void setup() {
    ReflectionTestUtils.setField(repo, "entityManager", em);
  }

  @Test
  void testGetProductByIdAndVersion() {
    when(contentRepo.findByVersionAndProductId(anyString(),anyString())).thenReturn(getMockProductModuleContent());
    List<String> expectedVersions = List.of("1.0", "1.1");
    mockProduct = new Product();
    mockProduct.setId(MOCK_PRODUCT_ID);
    mockProduct.setReleasedVersions(expectedVersions);

    TypedQuery<Product> query = mock(TypedQuery.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class);
    CriteriaQuery<Product> criteriaQuery = mock(CriteriaQuery.class);
    Root<Product> productRoot = mock(Root.class);

    when(em.getCriteriaBuilder()).thenReturn(cb);
    when(cb.createQuery(Product.class)).thenReturn(criteriaQuery);
    when(criteriaQuery.from(Product.class)).thenReturn(productRoot);
    when(em.createQuery(criteriaQuery)).thenReturn(query);
    when(query.getSingleResult()).thenReturn(mockProduct);


    Product actualProduct = repo.getProductByIdAndVersion(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION);
    assertEquals(mockProduct, actualProduct, "Expected the returned product to match the mocked product");
    assertThat(actualProduct.getProductModuleContent())
        .as("Expected product module content to match the mocked content")
        .usingRecursiveComparison()
        .isEqualTo(getMockProductModuleContent());
    verify(contentRepo).findByVersionAndProductId(anyString(),anyString());
  }

  @Test
  void testGetReleasedVersionsById() {
    List<String> expectedVersions = List.of("1.0", "1.1");
    mockProduct = new Product();
    mockProduct.setId(MOCK_PRODUCT_ID);
    mockProduct.setReleasedVersions(expectedVersions);

    TypedQuery<Product> query = mock(TypedQuery.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class);
    CriteriaQuery<Product> criteriaQuery = mock(CriteriaQuery.class);
    Root<Product> productRoot = mock(Root.class);

    when(em.getCriteriaBuilder()).thenReturn(cb);
    when(cb.createQuery(Product.class)).thenReturn(criteriaQuery);
    when(criteriaQuery.from(Product.class)).thenReturn(productRoot);
    when(em.createQuery(criteriaQuery)).thenReturn(query);
    when(query.getSingleResult()).thenReturn(mockProduct);

    List<String> actualReleasedVersions = repo.getReleasedVersionsById(MOCK_PRODUCT_ID);
    assertEquals(mockProduct.getReleasedVersions(), actualReleasedVersions,
        "Expected released versions returned from repository to match the product’s released versions");
  }

  @Test
  void testReleasedVersionsByIdWhenResultIsNull() {
    mockProduct = new Product();
    mockProduct.setId(MOCK_PRODUCT_ID);

    TypedQuery<Product> query = mock(TypedQuery.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class);
    CriteriaQuery<Product> criteriaQuery = mock(CriteriaQuery.class);
    Root<Product> productRoot = mock(Root.class);

    when(em.getCriteriaBuilder()).thenReturn(cb);
    when(cb.createQuery(Product.class)).thenReturn(criteriaQuery);
    when(criteriaQuery.from(Product.class)).thenReturn(productRoot);
    when(em.createQuery(criteriaQuery)).thenReturn(query);
    when(query.getSingleResult()).thenReturn(mockProduct);

    List<String> results = repo.getReleasedVersionsById(MOCK_PRODUCT_ID);
    assertEquals(0, results.size(),
        "Expected released versions list to be empty when the product has no released versions");
  }

  @Test
  void testFindProductByIdAndRelatedDataBuildsListedPredicateCriteria() {
    mockProduct = new Product();

    TypedQuery<Product> query = mock(TypedQuery.class);
    CriteriaBuilder cb = mock(CriteriaBuilder.class);
    CriteriaQuery<Product> criteriaQuery = mock(CriteriaQuery.class);
    Root<Product> productRoot = mock(Root.class);
    Path<Object> idPath = mock(Path.class);
    Path<Object> listedPath = mock(Path.class);
    Predicate idPredicate = mock(Predicate.class);
    Predicate notEqualListedFalsePredicate = mock(Predicate.class);
    Predicate listedIsNullPredicate = mock(Predicate.class);
    Predicate listedPredicate = mock(Predicate.class);

    when(em.getCriteriaBuilder()).thenReturn(cb);
    when(cb.createQuery(Product.class)).thenReturn(criteriaQuery);
    when(criteriaQuery.from(Product.class)).thenReturn(productRoot);
    when(productRoot.get(ID)).thenReturn(idPath);
    when(productRoot.get(LISTED)).thenReturn(listedPath);
    when(cb.equal(idPath, MOCK_PRODUCT_ID)).thenReturn(idPredicate);
    when(cb.notEqual(listedPath, false)).thenReturn(notEqualListedFalsePredicate);
    when(cb.isNull(listedPath)).thenReturn(listedIsNullPredicate);
    when(cb.or(notEqualListedFalsePredicate, listedIsNullPredicate)).thenReturn(listedPredicate);
    when(em.createQuery(criteriaQuery)).thenReturn(query);
    when(query.getSingleResult()).thenReturn(mockProduct);

    Product result = repo.findProductByIdAndRelatedData(MOCK_PRODUCT_ID);

    assertEquals(mockProduct, result, "Expected the returned product to match query single result");
    verify(cb).notEqual(listedPath, false);
    verify(cb).isNull(listedPath);
    verify(cb).or(notEqualListedFalsePredicate, listedIsNullPredicate);
    verify(criteriaQuery).where(idPredicate, listedPredicate);
  }
}
