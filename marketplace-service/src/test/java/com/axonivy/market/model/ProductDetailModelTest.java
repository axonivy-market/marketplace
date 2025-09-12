package com.axonivy.market.model;

import com.axonivy.market.entity.Product;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductDetailModelTest {

  @Test
  void testEqualsWithNullAndDifferentClass() {
    ProductDetailModel model1 = new ProductDetailModel();
    model1.setId("p1");

    assertNotEquals(model1, null, "Model should not equal null");
    assertNotEquals(model1, "string", "Model should not equal object of another class");
  }

  @Test
  void testEqualsSameId() {
    ProductDetailModel m1 = new ProductDetailModel();
    m1.setId("p1");
    ProductDetailModel m2 = new ProductDetailModel();
    m2.setId("p1");

    assertEquals(m1, m2, "Models with same ID should be equal");
    assertEquals(m1.hashCode(), m2.hashCode(), "Models with same ID should have same hashCode");
  }

  @Test
  void testEqualsDifferentId() {
    ProductDetailModel m1 = new ProductDetailModel();
    m1.setId("p1");
    ProductDetailModel m2 = new ProductDetailModel();
    m2.setId("p2");

    assertNotEquals(m1, m2, "Models with different IDs should not be equal");
    assertNotEquals(m1.hashCode(), m2.hashCode(),
        "Models with different IDs should ideally have different hashCodes");
  }

  @Test
  void testEqualsBothNullIds() {
    ProductDetailModel m1 = new ProductDetailModel();
    ProductDetailModel m2 = new ProductDetailModel();

    assertEquals(m1, m2, "Models with both IDs null should be equal");
    assertEquals(m1.hashCode(), m2.hashCode(),
        "Models with both IDs null should have the same hashCode");
  }

  @Test
  void testCreateDetailResourceNonProduction() {
    Product product = new Product();
    product.setVendor("Vendor");
    product.setVendorUrl("http://vendor.com");
    product.setNewestReleaseVersion("v1");
    product.setPlatformReview("5");
    product.setSourceUrl("http://src.com");
    product.setStatusBadgeUrl("http://badge.com");
    product.setLanguage("EN");
    product.setIndustry("IT");
    product.setContactUs(true);
    product.setDeprecated(false);
    product.setCost("Free");
    product.setInstallationCount(42);
    product.setCompatibilityRange("10.0+");
    product.setVendorImage("vendor.png");
    product.setVendorImageDarkMode("vendor-dark.png");
    product.setMavenDropins(true);

    ProductDetailModel model = new ProductDetailModel();
    ProductDetailModel.createDetailResource(model, product, false);

    assertEquals("Vendor", model.getVendor(), "Vendor should be copied");
    assertEquals("http://vendor.com", model.getVendorUrl(), "VendorUrl should be copied");
    assertEquals("v1", model.getNewestReleaseVersion(), "Newest release version should be copied");
    assertEquals("5", model.getPlatformReview(), "Platform review should be copied");
    assertEquals("http://src.com", model.getSourceUrl(), "Source URL should be copied");
    assertEquals("http://badge.com", model.getStatusBadgeUrl(), "Status badge should be copied");
    assertEquals("EN", model.getLanguage(), "Language should be copied");
    assertEquals("IT", model.getIndustry(), "Industry should be copied");
    assertTrue(model.getContactUs(), "ContactUs should be copied");
    assertFalse(model.getDeprecated(), "Deprecated should be copied");
    assertEquals("Free", model.getCost(), "Cost should be copied");
    assertEquals(42, model.getInstallationCount(), "Installation count should be copied");
    assertEquals("10.0+", model.getCompatibilityRange(), "Compatibility range should be copied");
    assertTrue(model.isMavenDropins(), "MavenDropins should be copied");

    // vendor image URLs (mocked transformation)
    assertNotNull(model.getVendorImage(), "Vendor image should be set for non-production");
    assertNotNull(model.getVendorImageDarkMode(), "Vendor dark image should be set for non-production");
  }

  @Test
  void testCreateDetailResourceProduction() {
    Product product = new Product();
    product.setVendorImage("vendor.png");
    product.setVendorImageDarkMode("vendor-dark.png");

    ProductDetailModel model = new ProductDetailModel();
    ProductDetailModel.createDetailResource(model, product, true);

    assertNotNull(model.getVendorImage(), "Vendor image should be set for production");
    assertNotNull(model.getVendorImageDarkMode(), "Vendor dark image should be set for production");
  }

  @Test
  void testCreateModelDelegatesCorrectly() {
    Product product = new Product();
    product.setId("p1");
    product.setVendor("Vendor");

    ProductDetailModel model = ProductDetailModel.createModel(product, false);

    assertEquals("p1", model.getId(), "ID should be copied from product");
    assertEquals("Vendor", model.getVendor(), "Vendor should be copied via createDetailResource");
  }
}
