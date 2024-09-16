package com.axonivy.market.service.impl;

import static com.axonivy.market.constants.CommonConstants.SLASH;
import static com.axonivy.market.constants.MetaConstants.META_FILE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHContent;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axonivy.market.entity.Image;
import com.axonivy.market.entity.Product;
import com.axonivy.market.repository.ImageRepository;

@ExtendWith(MockitoExtension.class)
class ImageServiceImplTest {
  @InjectMocks
  private ImageServiceImpl imageService;

  @Mock
  private ImageRepository imageRepository;

  @Captor
  ArgumentCaptor<Image> argumentCaptor = ArgumentCaptor.forClass(Image.class);

  @Test
  void testMappingImageFromGHContent() throws IOException {
    GHContent content = mock(GHContent.class);
    when(content.getSha()).thenReturn("914d9b6956db7a1404622f14265e435f36db81fa");
    when(content.getDownloadUrl()).thenReturn("https://raw.githubusercontent.com/images/comprehend-demo-sentiment.png");

    InputStream inputStream = this.getClass().getResourceAsStream(SLASH.concat(META_FILE));
    when(content.read()).thenReturn(inputStream);

    imageService.mappingImageFromGHContent(mockProduct(), content , true);

    Image expectedImage = new Image();
    expectedImage.setProductId("google-maps-connector");
    expectedImage.setSha("914d9b6956db7a1404622f14265e435f36db81fa");
    expectedImage.setImageUrl("https://raw.githubusercontent.com/images/comprehend-demo-sentiment.png");

    verify(imageRepository).save(argumentCaptor.capture());

    assertEquals(argumentCaptor.getValue().getProductId(),expectedImage.getProductId());
    assertEquals(argumentCaptor.getValue().getSha(),expectedImage.getSha());
    assertEquals(argumentCaptor.getValue().getImageUrl(),expectedImage.getImageUrl());
  }

  @Test
  void testMappingImageFromGHContent_noGhContent() {
    var result = imageService.mappingImageFromGHContent(mockProduct(), null , true);
    assertNull(result);
  }

  private Product mockProduct() {
    return Product.builder().id("google-maps-connector")
        .language("English")
        .build();
  }
}
