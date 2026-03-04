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
    ReleaseLetter releaseLetter = createMockReleaseLetter();
    ReleaseLetterModel result = releaseLetterModelAssembler.toModel(releaseLetter);

    assertNotNull(result, "Mapped ReleaseLetterModel should not be null");
    assertEquals("Release content", result.getContent(),
        "Content should be mapped correctly from ReleaseLetter");
    assertEquals("S43", result.getSprint(),
        "Sprint should be mapped correctly from ReleaseLetter");
    assertTrue(result.isLatest(),
        "Latest flag should be mapped correctly from ReleaseLetter");
    assertEquals(releaseLetter.getCreatedAt(), result.getCreatedAt(),
        "CreatedAt date should be mapped correctly from ReleaseLetter");
    assertEquals(releaseLetter.getUpdatedAt(), result.getUpdatedAt(),
            "UpdatedAt date should be mapped correctly from ReleaseLetter");
  }

  @Test
  void testToModelWithoutContent() {
    ReleaseLetter releaseLetter = createMockReleaseLetter();
    ReleaseLetterModel result = releaseLetterModelAssembler.toModelWithoutContent(releaseLetter);

    assertNotNull(result, "Mapped ReleaseLetterModel should not be null");
    assertEquals("S43", result.getSprint(),
        "Sprint should be mapped correctly from ReleaseLetter");
    assertTrue(result.isLatest(),
        "Latest flag should be mapped correctly from ReleaseLetter");
    assertEquals(releaseLetter.getCreatedAt(), result.getCreatedAt(),
            "CreatedAt date should be mapped correctly from ReleaseLetter");
    assertEquals(releaseLetter.getUpdatedAt(), result.getUpdatedAt(),
            "UpdatedAt date should be mapped correctly from ReleaseLetter");
  }

  private ReleaseLetter createMockReleaseLetter() {
    Date createdDate = new GregorianCalendar(2025, Calendar.FEBRUARY, 23, 10, 30, 0).getTime();
    Date updatedDate = new GregorianCalendar(2025, Calendar.FEBRUARY, 24, 10, 30, 0).getTime();

    ReleaseLetter releaseLetter = new ReleaseLetter();
    releaseLetter.setId("66e7efc8a24f36158df06fc7");
    releaseLetter.setContent("Release content");
    releaseLetter.setSprint("S43");
    releaseLetter.setLatest(true);
    releaseLetter.setCreatedAt(createdDate);
    releaseLetter.setUpdatedAt(updatedDate);

    return releaseLetter;
  }
}
