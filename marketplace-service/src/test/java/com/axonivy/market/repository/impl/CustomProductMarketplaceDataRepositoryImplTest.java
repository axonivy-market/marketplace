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
    ProductMarketplaceData productMarketplaceData = new ProductMarketplaceData();
    productMarketplaceData.setId(MOCK_PRODUCT_ID);
    productMarketplaceData.setInstallationCount(5);

    ProductMarketplaceData updatedProductMarketplaceData = new ProductMarketplaceData();
    updatedProductMarketplaceData.setId(MOCK_PRODUCT_ID);
    updatedProductMarketplaceData.setInstallationCount(6);

    when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class),
        eq(ProductMarketplaceData.class))).thenReturn(updatedProductMarketplaceData);

    int updatedCount = repo.increaseInstallationCount(MOCK_PRODUCT_ID);

    assertEquals(6, updatedCount);
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
    ProductMarketplaceData productMarketplaceData = new ProductMarketplaceData();
    productMarketplaceData.setId(MOCK_PRODUCT_ID);
    productMarketplaceData.setInstallationCount(1);

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


  @Test
  void testIncreaseInstallationCountForProductByDesignerVersion() {
    repo.increaseInstallationCountForProductByDesignerVersion(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION);
    verify(mongoTemplate).upsert(any(Query.class), any(Update.class), eq(ProductDesignerInstallation.class));
  }
}