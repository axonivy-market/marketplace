package com.axonivy.market.controller;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.assembler.ReleaseLetterModelAssembler;
import com.axonivy.market.entity.ReleaseLetter;
import com.axonivy.market.entity.ReleaseLetterDraft;
import com.axonivy.market.model.ReleaseLetterDraftModel;
import com.axonivy.market.model.ReleaseLetterModel;
import com.axonivy.market.model.ReleaseLetterModelRequest;
import com.axonivy.market.testutil.MockServletRequestUtils;
import com.axonivy.market.service.ReleaseLetterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReleaseLetterControllerTest extends BaseSetup {

  private static final String RELEASE_LETTER_SPRINT_NAME_SAMPLE = "DEMO";
  private static final String RELEASE_LETTER_CONTENT_SAMPLE = "Demo content";
  private static final String RELEASE_LETTER_ID_SAMPLE = "release-letter-id";
  private static final String GITHUB_USER_ID = "123456";

  @Mock
  private ReleaseLetterService releaseLetterService;

  @Mock
  private ReleaseLetterModelAssembler releaseLetterModelAssembler;

  @Mock
  private PagedResourcesAssembler<ReleaseLetter> pagedResourcesAssembler;

  private ReleaseLetterController releaseLetterController;

  @org.junit.jupiter.api.BeforeEach
  void setUp() {
    releaseLetterController = new ReleaseLetterController(
        releaseLetterService,
        releaseLetterModelAssembler,
        pagedResourcesAssembler);
  }

  @Test
  void testFindAllReleaseLettersShouldReturnPagedModelWhenDataExists() {
    PageRequest pageable = PageRequest.of(0, 20);
    ReleaseLetter mockReleaseLetter = createReleaseLetterMock();
    Page<ReleaseLetter> mockReleaseLetters = new PageImpl<>(List.of(mockReleaseLetter), pageable, 1);
    ReleaseLetterModel model = new ReleaseLetterModel();

    PagedModel<ReleaseLetterModel> pagedModel =
        PagedModel.of(List.of(model), new PagedModel.PageMetadata(1, 0, 1));

    when(releaseLetterService.findAllReleaseLetters(pageable, true)).thenReturn(mockReleaseLetters);
    when(pagedResourcesAssembler.toModel(mockReleaseLetters, releaseLetterModelAssembler)).thenReturn(pagedModel);

    ResponseEntity<PagedModel<ReleaseLetterModel>> response = releaseLetterController.findAllReleaseLetters(pageable);

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Response status should be 200 OK when release letters exist.");
    assertEquals(pagedModel, response.getBody(),
        "Response body should match paged model.");

    verify(releaseLetterService).findAllReleaseLetters(pageable, true);
    verify(pagedResourcesAssembler).toModel(mockReleaseLetters, releaseLetterModelAssembler);
  }

  @Test
  void testFindAllReleaseLettersShouldReturnEmptyPagedModelWhenNoData() {
    PageRequest pageable = PageRequest.of(0, 20);
    Page<ReleaseLetter> emptyPage = Page.empty();
    PagedModel<ReleaseLetterModel> emptyModel = PagedModel.empty();

    when(releaseLetterService.findAllReleaseLetters(pageable, true)).thenReturn(emptyPage);
    when(pagedResourcesAssembler.toEmptyModel(any(), any())).thenReturn(PagedModel.empty());

    ResponseEntity<PagedModel<ReleaseLetterModel>> response = releaseLetterController.findAllReleaseLetters(pageable);

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Response status should be 200 OK when empty page returned.");
    assertEquals(emptyModel, response.getBody(),
        "Response body should be empty.");

    verify(releaseLetterService).findAllReleaseLetters(pageable, true);
    verify(pagedResourcesAssembler).toEmptyModel(emptyPage, ReleaseLetterModel.class);
  }

  @Test
  void testFindReleaseLetterByIdShouldReturnReleaseLetter() {
    ReleaseLetter mockReleaseLetter = createReleaseLetterMock();
    ReleaseLetterModel model = new ReleaseLetterModel();

    when(releaseLetterService.findReleaseLetterById(RELEASE_LETTER_ID_SAMPLE)).thenReturn(mockReleaseLetter);
    when(releaseLetterModelAssembler.toModel(mockReleaseLetter)).thenReturn(model);

    var response = releaseLetterController.findReleaseLetterById(RELEASE_LETTER_ID_SAMPLE);

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Response status should be 200 OK when release letter is found.");
    assertTrue(response.hasBody(),
        "Response should contain a body when release letter is found.");

    verify(releaseLetterService).findReleaseLetterById(RELEASE_LETTER_ID_SAMPLE);
    verify(releaseLetterModelAssembler).toModel(mockReleaseLetter);
  }

  @Test
  void testFindLatestReleaseLetterShouldReturnPagedModelWhenDataExists() {
    PageRequest pageable = PageRequest.of(0, 20);
    ReleaseLetter mockReleaseLetter = createReleaseLetterMock();
    Page<ReleaseLetter> page = new PageImpl<>(List.of(mockReleaseLetter));

    ReleaseLetterModel model = new ReleaseLetterModel();
    PagedModel<ReleaseLetterModel> pagedModel =
        PagedModel.of(List.of(model), new PagedModel.PageMetadata(1, 0, 1));

    when(releaseLetterService.findLatestReleaseLetter(pageable)).thenReturn(page);
    when(pagedResourcesAssembler.toModel(page, releaseLetterModelAssembler))
        .thenReturn(pagedModel);

    var response = releaseLetterController.findLatestReleaseLetter(pageable);

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Response status should be 200 OK when all release letters are retrieved.");
    assertTrue(response.hasBody(),
        "Response should contain a body when all release letters are retrieved.");
    assertEquals(1, Objects.requireNonNull(response.getBody()).getContent().size(),
        "The number of release letters in the response body should match the expected count.");
  }


  @Test
  void testFindLatestReleaseLettersShouldReturnEmptyPagedModelWhenNoData() {
    PageRequest pageable = PageRequest.of(0, 20);
    Page<ReleaseLetter> emptyPage = Page.empty();

    PagedModel<ReleaseLetterModel> emptyModel = PagedModel.empty();

    when(releaseLetterService.findLatestReleaseLetter(pageable)).thenReturn(emptyPage);
    when(pagedResourcesAssembler.toEmptyModel(any(), any())).thenReturn(PagedModel.empty());

    var response = releaseLetterController.findLatestReleaseLetter(pageable);

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Response status should be 200 OK when empty page returned.");
    assertEquals(emptyModel, response.getBody(),
        "Response body should be empty.");

    verify(pagedResourcesAssembler)
        .toEmptyModel(emptyPage, ReleaseLetterModel.class);
  }

  @Test
  void testCreateReleaseLetterShouldReturnCreated() {
    ReleaseLetterModelRequest releaseLetterModelRequestMock = createReleaseLetterModelRequestMock();
    ReleaseLetter releaseLetterMock = createReleaseLetterMock();
    MockServletRequestUtils.createAndBindMockRequest();

    when(releaseLetterService.createReleaseLetter(releaseLetterModelRequestMock, false))
        .thenReturn(releaseLetterMock);

    var response = releaseLetterController.createReleaseLetter(releaseLetterModelRequestMock);

    assertEquals(HttpStatus.CREATED, response.getStatusCode(),
        "Response status should be 201 CREATED when a new release letter is successfully created.");
    assertTrue(
        Objects.requireNonNull(response.getHeaders().getLocation()).toString().contains(releaseLetterMock.getId()),
        "The Location header should contain the ID of the newly created release letter.");
  }

  @Test
  void testUpdateReleaseLetterShouldReturnUpdatedReleaseLetter() {
    String sprint = "S43";
    ReleaseLetterModelRequest releaseLetterModelRequestMock = createReleaseLetterModelRequestMock();
    ReleaseLetter releaseLetterMock = createReleaseLetterMock();
    ReleaseLetterModel model = new ReleaseLetterModel();
    UserInfo currentUser = createCurrentUser(GITHUB_USER_ID);

    when(releaseLetterService.updateReleaseLetter(sprint, releaseLetterModelRequestMock, GITHUB_USER_ID))
        .thenReturn(releaseLetterMock);
    when(releaseLetterModelAssembler.toModel(releaseLetterMock))
        .thenReturn(model);

    var response = releaseLetterController.updateReleaseLetter(sprint, releaseLetterModelRequestMock, currentUser);

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Response status should be 200 OK when a release letter is successfully updated.");
    assertTrue(response.hasBody(),
        "Response should contain a body after updating release letter.");
  }

  @Test
  void testFindAllReleaseLettersForManagementShouldUseAssemblerWithoutContent() {
    PageRequest pageable = PageRequest.of(0, 20);
    ReleaseLetter mockReleaseLetter = createReleaseLetterMock();
    Page<ReleaseLetter> page = new PageImpl<>(List.of(mockReleaseLetter), pageable, 1);
    ReleaseLetterModel model = new ReleaseLetterModel();
    model.setId("release-letter-id");
    PagedModel<ReleaseLetterModel> pagedModel =
        PagedModel.of(List.of(model), new PagedModel.PageMetadata(1, 0, 1));
    UserInfo currentUser = createCurrentUser("github-user-id");
    when(releaseLetterService.findAllReleaseLetters(pageable, false)).thenReturn(page);
    when(pagedResourcesAssembler.toModel(eq(page), any(RepresentationModelAssembler.class))).thenReturn(pagedModel);
    when(releaseLetterService.getDraftContentByGitHubUserIdAndReleaseLetterId(anyString(), anyString()))
        .thenReturn(null);

    ResponseEntity<PagedModel<ReleaseLetterModel>> response =
        releaseLetterController.findAllReleaseLettersForManagement(pageable, currentUser);

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Response status should be 200 OK.");
    assertEquals(pagedModel, response.getBody(),
        "Response body should match paged model returned by assembler.");

    verify(releaseLetterService).findAllReleaseLetters(pageable, false);
    verify(pagedResourcesAssembler).toModel(eq(page), any(RepresentationModelAssembler.class));
  }

  @Test
  void testDeleteReleaseLetterShouldCallService() {
    releaseLetterController.deleteReleaseLetter(RELEASE_LETTER_ID_SAMPLE);
    verify(releaseLetterService).deleteReleaseLetterById(RELEASE_LETTER_ID_SAMPLE);
  }

  @Test
  void testSaveAsDraftShouldReturnDraftSuccessfully() {
    ReleaseLetterModelRequest releaseLetterModelRequestMock = createReleaseLetterModelRequestMock();

    ReleaseLetterDraftModel releaseLetterDraftModel = ReleaseLetterDraftModel.builder()
        .id("draft-id")
        .releaseLetterId(RELEASE_LETTER_ID_SAMPLE)
        .draftContent(RELEASE_LETTER_CONTENT_SAMPLE)
        .build();
    UserInfo currentUser = createCurrentUser(GITHUB_USER_ID);
    when(releaseLetterService.saveAsDraft(releaseLetterModelRequestMock, GITHUB_USER_ID))
        .thenReturn(releaseLetterDraftModel);
    ResponseEntity<ReleaseLetterDraftModel> response =
        releaseLetterController.saveAsDraft(releaseLetterModelRequestMock, currentUser);

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Response status should be 200 OK when saving draft succeeds.");
    assertNotNull(response.getBody(),
        "Response body should contain the saved draft.");
    assertEquals(releaseLetterDraftModel, response.getBody(),
        "Response body should match the saved draft returned from service.");
    verify(releaseLetterService)
        .saveAsDraft(releaseLetterModelRequestMock, GITHUB_USER_ID);
  }

  @Test
  void testGetDraftShouldReturnDraftModelWhenDraftExists() {
    ReleaseLetterDraft releaseLetterDraft = new ReleaseLetterDraft();
    releaseLetterDraft.setId("draft-id");
    releaseLetterDraft.setReleaseLetterId(RELEASE_LETTER_ID_SAMPLE);
    releaseLetterDraft.setDraftContent(RELEASE_LETTER_CONTENT_SAMPLE);
    UserInfo currentUser = createCurrentUser(GITHUB_USER_ID);

    when(releaseLetterService.getDraftContentByGitHubUserIdAndReleaseLetterId(
        GITHUB_USER_ID,
        RELEASE_LETTER_ID_SAMPLE
    )).thenReturn(releaseLetterDraft);

    ResponseEntity<ReleaseLetterDraftModel> response =
        releaseLetterController.getDraft(RELEASE_LETTER_ID_SAMPLE, currentUser);

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Response status should be 200 OK when draft exists.");
    assertNotNull(response.getBody(),
        "Response body should contain the draft model when draft exists.");
    assertEquals(releaseLetterDraft.getId(), response.getBody().getId(),
        "Draft model ID should match the draft entity ID.");
    assertEquals(releaseLetterDraft.getReleaseLetterId(), response.getBody().getReleaseLetterId(),
        "Release letter ID should match the draft entity.");
    assertEquals(releaseLetterDraft.getDraftContent(), response.getBody().getDraftContent(),
        "Draft content should match the draft entity.");
    verify(releaseLetterService)
        .getDraftContentByGitHubUserIdAndReleaseLetterId(
            GITHUB_USER_ID,
            RELEASE_LETTER_ID_SAMPLE
        );
  }

  @Test
  void testGetDraftShouldReturnNullBodyWhenDraftDoesNotExist() {
    UserInfo currentUser = createCurrentUser(GITHUB_USER_ID);
    when(releaseLetterService.getDraftContentByGitHubUserIdAndReleaseLetterId(
        GITHUB_USER_ID,
        RELEASE_LETTER_ID_SAMPLE
    )).thenReturn(null);

    ResponseEntity<ReleaseLetterDraftModel> response =
        releaseLetterController.getDraft(RELEASE_LETTER_ID_SAMPLE, currentUser);
    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Response status should be 200 OK when no draft exists.");
    assertNull(response.getBody(),
        "Response body should be null when no draft exists.");
    verify(releaseLetterService)
        .getDraftContentByGitHubUserIdAndReleaseLetterId(
            GITHUB_USER_ID,
            RELEASE_LETTER_ID_SAMPLE
        );
  }

  private UserInfo createCurrentUser(String userId) {
    var currentUser = new UserInfo();
    currentUser.setId(userId);
    currentUser.setUsername("test-user");
    return currentUser;
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
}
