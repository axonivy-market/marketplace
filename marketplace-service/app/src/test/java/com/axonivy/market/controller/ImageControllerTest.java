package com.axonivy.market.controller;

import com.axonivy.market.service.ImageService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerWebMvcTest(ImageController.class)
class ImageControllerTest extends WebMvcControllerTestSupport {

  @MockitoBean
  private ImageService imageService;

  @Test
  void testGetImageFromId() throws Exception {
    byte[] mockImageData = "image data".getBytes();
    when(imageService.readImage("66e2b14868f2f95b2f95549a")).thenReturn(mockImageData);

    mockMvc.perform(get("/api/image/{id}", "66e2b14868f2f95b2f95549a"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.IMAGE_PNG))
        .andExpect(content().bytes(mockImageData));
  }

  @Test
  void testGetImageFromIdWhenImageNotFound() throws Exception {
    when(imageService.readImage("missing-id")).thenReturn(null);

    mockMvc.perform(get("/api/image/{id}", "missing-id"))
        .andExpect(status().isNotFound());
  }

  @Test
  void testGetImageFromIdWhenImageEmpty() throws Exception {
    when(imageService.readImage("empty-id")).thenReturn(new byte[0]);

    mockMvc.perform(get("/api/image/{id}", "empty-id"))
        .andExpect(status().isNoContent());
  }
}
