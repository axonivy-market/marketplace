package com.axonivy.market.constants;

import java.util.Map;

public class MongoDBConstants {

    private MongoDBConstants() {
    }

    public static final String ID ="_id";
    public static final String ADD_FIELD ="$addFields";
    public static final String PRODUCT_MODULE_CONTENTS ="productModuleContents";
    public static final String PRODUCT_MODULE_CONTENTS_KEY_FIELD ="productModuleContents.$id";
    public static final String PRODUCT_MODULE_CONTENT ="productModuleContent";
    public static final String PRODUCT_MODULE_CONTENT_QUERY ="$productModuleContents";
    public static final String FILTER ="$filter";
    public static final String INPUT ="input";
    public static final String AS ="as";
    public static final String CONDITION ="cond";
    public static final String EQUAL ="$eq";
    public static final String PRODUCT_MODULE_CONTENT_TAG ="$$productModuleContent.tag";
    public static final String PRODUCT_MODULE_CONTENT_FILTER_TAG ="productModuleContent.tag";
    public static final String PRODUCT_COLLECTION ="Product";
    public static final String NEWEST_RELEASED_VERSION_QUERY = "$newestReleaseVersion";
    public static final String PRODUCT_MODULE_CONTENT_DOCUMENT ="ProductModuleContent";
    public static final String UNWIND = "$unwind";
    public static final String LOOKUP = "$lookup";
    public static final String MATCH = "$match";
    public static final String FROM = "from";
    public static final String LOCAL_FIELD = "localField";
    public static final String FOREIGN_FIELD = "foreignField";
}
