package com.axonivy.market.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AppController.class)
class AppControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void testRoot() throws Exception {
    var response = mockMvc.perform(get("/"));
    response.andExpect(status().isOk());
    response.andExpect(jsonPath("$.messageDetails").value("Welcome to Marketplace API"));
  }

}
