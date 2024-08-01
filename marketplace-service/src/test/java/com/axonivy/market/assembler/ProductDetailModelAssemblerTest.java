package com.axonivy.market.assembler;

import com.axonivy.market.enums.NonStandardProduct;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
class ProductDetailModelAssemblerTest {
  @InjectMocks
  private ProductDetailModelAssembler assembler;

  @BeforeEach
  void setup() {
    assembler = new ProductDetailModelAssembler(new ProductModelAssembler());
  }

  @Test
  void testConvertVersionToTag() {
    
    String rawVersion = StringUtils.EMPTY;
    Assertions.assertEquals(rawVersion, assembler.convertVersionToTag(StringUtils.EMPTY, rawVersion));

    rawVersion = "Version 11.0.0";
    String targetVersion = "11.0.0";
    Assertions.assertEquals(targetVersion, assembler.convertVersionToTag(NonStandardProduct.PORTAL.getId(), rawVersion));

    targetVersion = "v11.0.0";
    Assertions.assertEquals(targetVersion, assembler.convertVersionToTag(NonStandardProduct.GRAPHQL_DEMO.getId(), rawVersion));
  }
}
