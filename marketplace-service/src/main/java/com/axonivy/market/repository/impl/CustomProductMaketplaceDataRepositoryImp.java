package com.axonivy.market.repository.impl;

import com.axonivy.market.constants.EntityConstants;
import com.axonivy.market.constants.MongoDBConstants;
import com.axonivy.market.criteria.ProductSearchCriteria;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductDesignerInstallation;
import com.axonivy.market.entity.ProductMarketplaceData;
import com.axonivy.market.entity.ProductModuleContent;
import com.axonivy.market.enums.DocumentField;
import com.axonivy.market.enums.Language;
import com.axonivy.market.enums.TypeOption;
import com.axonivy.market.repository.CustomProductMarketplaceDataRepository;
import com.axonivy.market.repository.CustomProductRepository;
import com.axonivy.market.repository.CustomRepository;
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
public class CustomProductMaketplaceDataRepositoryImp extends CustomRepository implements CustomProductMarketplaceDataRepository {

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
