package com.axonivy.market.constants;

public class MongoDBConstants {
    private MongoDBConstants() {
    }
    
    public static final String ID ="_id";
    public static final String ADD_FIELD ="$addFields";
    public static final String PRODUCT_MODULE_CONTENTS ="productModuleContents";
    public static final String PRODUCT_MODULE_CONTENT ="productModuleContent";
    public static final String PRODUCT_MODULE_CONTENT_QUERY ="$productModuleContents";
    public static final String FILTER ="$filter";
    public static final String INPUT ="input";
    public static final String AS ="as";
    public static final String CONDITION ="cond";
    public static final String EQUAL ="$eq";
    public static final String PRODUCT_MODULE_CONTENT_TAG ="$$productModuleContent.tag";
    public static final String PRODUCT_COLLECTION ="Product";
    public static final String NEWEST_RELEASED_VERSION_QUERY = "$newestReleaseVersion";
}
