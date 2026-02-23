package com.axonivy.market.assembler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReleaseLetterModelAssemblerTest {
  @InjectMocks
  private ReleaseLetterModelAssembler releaseLetterModelAssembler;

  @BeforeEach
  void setup() {
    releaseLetterModelAssembler = new ReleaseLetterModelAssembler();
  }

  @Test
  void testToModel() {

  }
}
