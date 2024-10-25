package com.axonivy.market.repository.impl;

import com.axonivy.market.constants.MongoDBConstants;
import com.axonivy.market.entity.ProductDesignerInstallation;
import com.axonivy.market.entity.ProductMarketplaceData;
import com.axonivy.market.repository.CustomProductMarketplaceDataRepository;
import com.axonivy.market.repository.CustomRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@Builder
@AllArgsConstructor
public class CustomProductMarketplaceDataRepositoryImp extends CustomRepository implements CustomProductMarketplaceDataRepository {

  final MongoTemplate mongoTemplate;

  @Override
  public int updateInitialCount(String productId, int initialCount) {
    Update update = new Update().inc(MongoDBConstants.INSTALLATION_COUNT, initialCount).set(
        MongoDBConstants.SYNCHRONIZED_INSTALLATION_COUNT, true);
    ProductMarketplaceData updatedProductMarketplaceData = mongoTemplate.findAndModify(createQueryById(productId),
        update, FindAndModifyOptions.options().returnNew(true), ProductMarketplaceData.class);
    return updatedProductMarketplaceData != null ? updatedProductMarketplaceData.getInstallationCount() : 0;
  }

  @Override
  public int increaseInstallationCount(String productId) {
    Update update = new Update().inc(MongoDBConstants.INSTALLATION_COUNT, 1);
    ProductMarketplaceData updatedProduct = mongoTemplate.findAndModify(createQueryById(productId), update,
        FindAndModifyOptions.options().returnNew(true), ProductMarketplaceData.class);
    return updatedProduct != null ? updatedProduct.getInstallationCount() : 0;
  }

  @Override
  public void increaseInstallationCountForProductByDesignerVersion(String productId, String designerVersion) {
    Update update = new Update().inc(MongoDBConstants.INSTALLATION_COUNT, 1);
    mongoTemplate.upsert(createQueryByProductIdAndDesignerVersion(productId, designerVersion),
        update, ProductDesignerInstallation.class);
  }

  private Query createQueryByProductIdAndDesignerVersion(String productId, String designerVersion) {
    return new Query(Criteria.where(MongoDBConstants.PRODUCT_ID).is(productId)
        .andOperator(Criteria.where(MongoDBConstants.DESIGNER_VERSION).is(designerVersion)));
  }
}
