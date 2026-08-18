package com.axonivy.market.stable.controller;

import com.axonivy.market.core.service.CoreImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ImageControllerMockMvcTest {

  private static final String IMAGE_ID = "66e7efc8a24f36158df06fc7";

  private MockMvc mockMvc;

  @Mock
  private CoreImageService coreImageService;

  @BeforeEach
  void setUp() {
    ImageController imageController = new ImageController(coreImageService);
    mockMvc = MockMvcBuilders.standaloneSetup(imageController).build();
  }

  @Test
  void shouldReturnImageContentAsPng() throws Exception {
    byte[] imageData = "fake-png-content".getBytes(StandardCharsets.UTF_8);
    when(coreImageService.readImage(IMAGE_ID)).thenReturn(imageData);

    mockMvc.perform(get("/api/image/" + IMAGE_ID))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.IMAGE_PNG))
        .andExpect(content().bytes(imageData));

    verify(coreImageService).readImage(IMAGE_ID);
  }

  @Test
  void shouldReturnNotFoundWhenImageIsMissing() throws Exception {
    when(coreImageService.readImage(IMAGE_ID)).thenReturn(null);

    mockMvc.perform(get("/api/image/" + IMAGE_ID))
        .andExpect(status().isNotFound());

    verify(coreImageService).readImage(IMAGE_ID);
  }

  @Test
  void shouldReturnNoContentWhenImageIsEmpty() throws Exception {
    when(coreImageService.readImage(IMAGE_ID)).thenReturn(new byte[0]);

    mockMvc.perform(get("/api/image/" + IMAGE_ID))
        .andExpect(status().isNoContent());

    verify(coreImageService).readImage(IMAGE_ID);
  }
}
