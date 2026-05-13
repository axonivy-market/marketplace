package com.axonivy.market.model;

import com.axonivy.market.entity.ReleaseLetterDraft;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReleaseLetterDraftModelTest {
  @Test
  void shouldBuildReleaseLetterDraftModelFromEntity() {
    ReleaseLetterDraft releaseLetterDraft = new ReleaseLetterDraft();
    releaseLetterDraft.setId("draft-id");
    releaseLetterDraft.setReleaseLetterId("release-letter-id");
    releaseLetterDraft.setDraftContent("Draft content");

    ReleaseLetterDraftModel result = ReleaseLetterDraftModel.from(releaseLetterDraft);

    assertNotNull(result, "Result should not be null");
    assertEquals("draft-id", result.getId(), "Id should match the entity id");
    assertEquals("release-letter-id", result.getReleaseLetterId(),
        "Release letter id should match the entity release letter id");
    assertEquals("Draft content", result.getDraftContent(),
        "Draft content should match the entity draft content");
  }

  @Test
  void shouldCreateModelUsingBuilder() {
    ReleaseLetterDraftModel model = ReleaseLetterDraftModel.builder()
        .id("id")
        .releaseLetterId("release-id")
        .draftContent("content")
        .build();

    assertEquals("id", model.getId(),
        "Id should match the builder value");
    assertEquals("release-id", model.getReleaseLetterId(),
        "Release letter id should match the builder value");
    assertEquals("content", model.getDraftContent(),
        "Draft content should match the builder value");
  }

  @Test
  void shouldCreateModelUsingAllArgsConstructor() {
    ReleaseLetterDraftModel model =
        new ReleaseLetterDraftModel("id", "release-id", "content");

    assertEquals("id", model.getId(),
        "Id should match the constructor value");
    assertEquals("release-id", model.getReleaseLetterId(),
        "Release letter id should match the constructor value");
    assertEquals("content", model.getDraftContent(),
        "Draft content should match the constructor value");
  }

  @Test
  void shouldSetAndGetValues() {
    ReleaseLetterDraftModel model = new ReleaseLetterDraftModel();

    model.setId("id");
    model.setReleaseLetterId("release-id");
    model.setDraftContent("content");

    assertEquals("id", model.getId(),
        "Id should match the set value");
    assertEquals("release-id", model.getReleaseLetterId(),
        "Release letter id should match the set value");
    assertEquals("content", model.getDraftContent(),
        "Draft content should match the set value");
  }
}
