package com.axonivy.market.controller;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.aop.aspect.AuthorizedAspect;
import com.axonivy.market.assembler.ReleaseLetterModelAssembler;
import com.axonivy.market.entity.ReleaseLetter;
import com.axonivy.market.entity.ReleaseLetterDraft;
import com.axonivy.market.model.ReleaseLetterDraftModel;
import com.axonivy.market.model.ReleaseLetterModel;
import com.axonivy.market.model.ReleaseLetterModelRequest;
import com.axonivy.market.service.ReleaseLetterService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerWebMvcTest(ReleaseLetterController.class)
class ReleaseLetterControllerTest extends WebMvcControllerTestSupport {

  private static final String RELEASE_LETTER_SPRINT_NAME_SAMPLE = "DEMO";
  private static final String RELEASE_LETTER_CONTENT_SAMPLE = "Demo content";
  private static final String RELEASE_LETTER_ID_SAMPLE = "release-letter-id";
  private static final String GITHUB_USER_ID = "123456";

  private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

  @MockitoBean
  private ReleaseLetterService releaseLetterService;

  @MockitoBean
  private ReleaseLetterModelAssembler releaseLetterModelAssembler;

  @MockitoBean
  private PagedResourcesAssembler<ReleaseLetter> pagedResourcesAssembler;

  @Test
  void testFindAllReleaseLettersShouldReturnPagedModelWhenDataExists() throws Exception {
    PageRequest pageable = PageRequest.of(0, 20);
    ReleaseLetter mockReleaseLetter = createReleaseLetterMock();
    Page<ReleaseLetter> mockReleaseLetters = new PageImpl<>(List.of(mockReleaseLetter), pageable, 1);
    ReleaseLetterModel model = createReleaseLetterModelMock();

    when(releaseLetterService.findAllReleaseLetters(pageable, true)).thenReturn(mockReleaseLetters);
    when(pagedResourcesAssembler.toModel(mockReleaseLetters, releaseLetterModelAssembler))
        .thenReturn(PagedModel.of(List.of(model), new PagedModel.PageMetadata(1, 0, 1)));

    mockMvc.perform(get("/api/release-letters").param("page", "0").param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$..id").value(hasItem(RELEASE_LETTER_ID_SAMPLE)));
  }

  @Test
  void testCreateReleaseLetterShouldReturnCreated() throws Exception {
    ReleaseLetterModelRequest request = createReleaseLetterModelRequestMock();
    ReleaseLetter releaseLetterMock = createReleaseLetterMock();
    when(releaseLetterService.createReleaseLetter(any(), eq(false))).thenReturn(releaseLetterMock);

    mockMvc.perform(post("/api/release-letters")
            .with(requestedByHeader())
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", org.hamcrest.Matchers.containsString(RELEASE_LETTER_ID_SAMPLE)));
  }

  @Test
  void testUpdateReleaseLetterShouldReturnUpdatedReleaseLetter() throws Exception {
    String sprint = "S43";
    ReleaseLetterModelRequest request = createReleaseLetterModelRequestMock();
    ReleaseLetter releaseLetterMock = createReleaseLetterMock();
    ReleaseLetterModel model = createReleaseLetterModelMock();

    when(releaseLetterService.updateReleaseLetter(eq(sprint), any(), eq(GITHUB_USER_ID))).thenReturn(releaseLetterMock);
    when(releaseLetterModelAssembler.toModel(releaseLetterMock)).thenReturn(model);

    mockMvc.perform(put("/api/release-letters/{id}", sprint)
            .with(requestedByHeader())
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .requestAttr(AuthorizedAspect.GITHUB_USER_ID_ATTRIBUTE, GITHUB_USER_ID)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(RELEASE_LETTER_ID_SAMPLE));
  }

  @Test
  void testGetDraftShouldReturnDraftModelWhenDraftExists() throws Exception {
    ReleaseLetterDraft releaseLetterDraft = new ReleaseLetterDraft();
    releaseLetterDraft.setId("draft-id");
    releaseLetterDraft.setReleaseLetterId(RELEASE_LETTER_ID_SAMPLE);
    releaseLetterDraft.setDraftContent(RELEASE_LETTER_CONTENT_SAMPLE);

    when(releaseLetterService.getDraftContentByGitHubUserIdAndReleaseLetterId(GITHUB_USER_ID, RELEASE_LETTER_ID_SAMPLE))
        .thenReturn(releaseLetterDraft);

    mockMvc.perform(get("/api/release-letters/{id}/draft", RELEASE_LETTER_ID_SAMPLE)
            .requestAttr(AuthorizedAspect.GITHUB_USER_ID_ATTRIBUTE, GITHUB_USER_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.releaseLetterId").value(RELEASE_LETTER_ID_SAMPLE));
  }

  @Test
  void testDeleteReleaseLetterShouldCallService() throws Exception {
    mockMvc.perform(delete("/api/release-letters/{id}", RELEASE_LETTER_ID_SAMPLE)
            .with(requestedByHeader()))
        .andExpect(status().isOk());
  }

  private ReleaseLetter createReleaseLetterMock() {
    ReleaseLetter mockReleaseLetter = new ReleaseLetter();
    mockReleaseLetter.setSprint(RELEASE_LETTER_SPRINT_NAME_SAMPLE);
    mockReleaseLetter.setContent(RELEASE_LETTER_CONTENT_SAMPLE);
    mockReleaseLetter.setId(RELEASE_LETTER_ID_SAMPLE);
    mockReleaseLetter.setLatest(true);
    return mockReleaseLetter;
  }

  private ReleaseLetterModelRequest createReleaseLetterModelRequestMock() {
    ReleaseLetterModelRequest mockReleaseLetterModelRequest = new ReleaseLetterModelRequest();
    mockReleaseLetterModelRequest.setSprint(RELEASE_LETTER_SPRINT_NAME_SAMPLE);
    mockReleaseLetterModelRequest.setContent(RELEASE_LETTER_CONTENT_SAMPLE);
    mockReleaseLetterModelRequest.setLatest(true);
    return mockReleaseLetterModelRequest;
  }

  private ReleaseLetterModel createReleaseLetterModelMock() {
    ReleaseLetterModel model = new ReleaseLetterModel();
    model.setId(RELEASE_LETTER_ID_SAMPLE);
    return model;
  }
}
