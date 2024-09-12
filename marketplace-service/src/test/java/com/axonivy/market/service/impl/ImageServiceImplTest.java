package com.axonivy.market.service.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.entity.Image;
import com.axonivy.market.entity.Product;
import com.axonivy.market.repository.ImageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHContent;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.axonivy.market.constants.CommonConstants.SLASH;
import static com.axonivy.market.constants.MetaConstants.META_FILE;
import static com.axonivy.market.enums.DocumentField.SHORT_DESCRIPTIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ImageServiceImplTest {
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

    imageService.mappingImageFromGHContent(mockProduct(), content);

    Image expectedImage = new Image();
    expectedImage.setProductId("google-maps-connector");
    expectedImage.setSha("914d9b6956db7a1404622f14265e435f36db81fa");
    expectedImage.setLogoUrl("https://raw.githubusercontent.com/images/comprehend-demo-sentiment.png");

    verify(imageRepository).save(argumentCaptor.capture());

    assertEquals(argumentCaptor.getValue().getProductId(),expectedImage.getProductId());
    assertEquals(argumentCaptor.getValue().getSha(),expectedImage.getSha());
    assertEquals(argumentCaptor.getValue().getLogoUrl(),expectedImage.getLogoUrl());
  }

  private Product mockProduct() {
    return Product.builder().id("google-maps-connector")
        .language("English")
        .build();
  }
}
