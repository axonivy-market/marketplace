package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MongoDBConstants {
  public static final String ID = "_id";
  public static final String PRODUCT_COLLECTION = "Product";
  public static final String PRODUCT_MARKETPLACE_COLLECTION = "ProductMarketplaceData";
  public static final String MARKETPLACE_DATA = "marketplaceData";
  public static final String INSTALLATION_COUNT = "InstallationCount";
  public static final String SYNCHRONIZED_INSTALLATION_COUNT = "SynchronizedInstallationCount";
  public static final String PRODUCT_ID = "productId";
  public static final String DESIGNER_VERSION = "designerVersion";
  public static final String VERSION = "version";
  public static final String RELEASED_VERSIONS = "releasedVersions";
  public static final String ARTIFACTS = "artifacts";
  public static final String ARTIFACTS_DOC = "artifacts.doc";
}
