package com.axonivy.market.assembler;

import com.axonivy.market.entity.ReleaseLetter;
import com.axonivy.market.model.ReleaseLetterModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.jupiter.api.Assertions.*;

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
    Date createdDate = new GregorianCalendar(2025, Calendar.FEBRUARY, 23, 10, 30, 0).getTime();

    ReleaseLetter releaseLetter = new ReleaseLetter();
    releaseLetter.setContent("Release content");
    releaseLetter.setSprint("S43");
    releaseLetter.setLatest(true);
    releaseLetter.setCreatedAt(createdDate);

    ReleaseLetterModel result = releaseLetterModelAssembler.toModel(releaseLetter);

    assertNotNull(result, "Mapped ReleaseLetterModel should not be null");
    assertEquals("Release content", result.getContent(),
        "Content should be mapped correctly from ReleaseLetter");

    assertEquals("S43", result.getSprint(),
        "Sprint should be mapped correctly from ReleaseLetter");

    assertTrue(result.isLatest(),
        "Latest flag should be mapped correctly from ReleaseLetter");

    assertEquals(createdDate, result.getCreatedAt(),
        "CreatedAt date should be mapped correctly from ReleaseLetter");
  }
}
