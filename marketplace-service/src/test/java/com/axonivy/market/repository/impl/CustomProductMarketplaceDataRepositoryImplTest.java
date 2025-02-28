package com.axonivy.market.repository.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.constants.MongoDBConstants;
import com.axonivy.market.entity.ProductDesignerInstallation;
import com.axonivy.market.entity.ProductMarketplaceData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomProductMarketplaceDataRepositoryImplTest extends BaseSetup {
  @Mock
  private MongoTemplate mongoTemplate;
  @InjectMocks
  private CustomProductMarketplaceDataRepositoryImpl repo;

  @Test
  void testIncreaseInstallationCount() {
    ProductMarketplaceData mockProductMarketplaceData = getMockProductMarketplaceData();
    when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class),
        eq(ProductMarketplaceData.class))).thenReturn(mockProductMarketplaceData);

    int updatedCount = repo.increaseInstallationCount(MOCK_PRODUCT_ID);

    assertEquals(3, updatedCount);
  }

  @Test
  void testIncreaseInstallationCount_NullProduct() {
    when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class),
        eq(ProductMarketplaceData.class))).thenReturn(null);
    int updatedCount = repo.increaseInstallationCount(MOCK_PRODUCT_ID);
    assertEquals(0, updatedCount);
  }

  @Test
  void testUpdateInitialCount() {
    int initialCount = 10;

    ProductMarketplaceData updatedProductMarketplaceData = new ProductMarketplaceData();
    updatedProductMarketplaceData.setId(MOCK_PRODUCT_ID);
    updatedProductMarketplaceData.setInstallationCount(11);

    when(mongoTemplate.findAndModify(any(Query.class),
        eq(new Update().inc(MongoDBConstants.INSTALLATION_COUNT, initialCount)
            .set(MongoDBConstants.SYNCHRONIZED_INSTALLATION_COUNT, true)),
        any(FindAndModifyOptions.class),
        eq(ProductMarketplaceData.class))
    ).thenReturn(updatedProductMarketplaceData);

    int updatedCount = repo.updateInitialCount(MOCK_PRODUCT_ID, initialCount);

    assertEquals(11, updatedCount);
  }

//  @Test
//  void testIncreaseInstallationCountForProductByDesignerVersion() {
//    repo.increaseInstallationCountForProductByDesignerVersion(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION);
//    verify(mongoTemplate).upsert(any(Query.class), any(Update.class), eq(ProductDesignerInstallation.class));
//  }

  @Test
  void testCheckAndInitProductMarketplaceDataIfNotExist(){
    Query query = new Query(Criteria.where(MongoDBConstants.ID).is(MOCK_PRODUCT_ID));
    when(mongoTemplate.exists(query, ProductMarketplaceData.class)).thenReturn(true);
    repo.checkAndInitProductMarketplaceDataIfNotExist(MOCK_PRODUCT_ID);
    verify(mongoTemplate, never()).insert(any(ProductMarketplaceData.class));

    when(mongoTemplate.exists(query, ProductMarketplaceData.class)).thenReturn(false);
    repo.checkAndInitProductMarketplaceDataIfNotExist(MOCK_PRODUCT_ID);
    verify(mongoTemplate, times(1)).insert(any(ProductMarketplaceData.class));
  }
}