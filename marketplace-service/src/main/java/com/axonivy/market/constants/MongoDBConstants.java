package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MongoDBConstants {

  public static final String ID = "_id";
  public static final String PRODUCT_COLLECTION = "Product";
  public static final String INSTALLATION_COUNT = "InstallationCount";
  public static final String SYNCHRONIZED_INSTALLATION_COUNT = "SynchronizedInstallationCount";
  public static final String PRODUCT_ID = "productId";
  public static final String DESIGNER_VERSION = "designerVersion";
  public static final String TAG = "tag";
  public static final String ARTIFACTS_DOC = "artifacts.doc";
}
