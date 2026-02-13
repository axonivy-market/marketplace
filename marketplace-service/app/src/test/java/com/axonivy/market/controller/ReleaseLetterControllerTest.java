package com.axonivy.market.controller;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.assembler.ReleaseLetterModelAssembler;
import com.axonivy.market.entity.ReleaseLetter;
import com.axonivy.market.model.ReleaseLetterModel;
import com.axonivy.market.model.ReleaseLetterModelRequest;
import com.axonivy.market.service.ReleaseLetterService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReleaseLetterControllerTest extends BaseSetup {

  private static final String RELEASE_LETTER_SPRINT_NAME_SAMPLE = "DEMO";
  private static final String RELEASE_LETTER_CONTENT_SAMPLE = "Demo content";
  private static final String RELEASE_LETTER_ID_SAMPLE = "release-letter-id";

  @Mock
  private ReleaseLetterService releaseLetterService;

  @Mock
  private ReleaseLetterModelAssembler releaseLetterModelAssembler;

  @Mock
  private PagedResourcesAssembler<ReleaseLetter> pagedResourcesAssembler;

  @InjectMocks
  private ReleaseLetterController releaseLetterController;

  @Test
  void findAllReleaseLettersShouldReturnPagedModelWhenDataExists() {
    PageRequest pageable = PageRequest.of(0, 20);
    ReleaseLetter mockReleaseLetter = createReleaseLetterMock();

    Page<ReleaseLetter> mockReleaseLetters = new PageImpl<>(List.of(mockReleaseLetter), pageable, 1);

    ReleaseLetterModel model = new ReleaseLetterModel();
    PagedModel<ReleaseLetterModel> pagedModel =
        PagedModel.of(List.of(model), new PagedModel.PageMetadata(1, 0, 1));

    when(releaseLetterService.findAllReleaseLetters(pageable)).thenReturn(mockReleaseLetters);
    when(pagedResourcesAssembler.toModel(mockReleaseLetters, releaseLetterModelAssembler))
        .thenReturn(pagedModel);

    ResponseEntity<PagedModel<ReleaseLetterModel>> response =
        releaseLetterController.findAllReleaseLetters(pageable);

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Response status should be 200 OK when release letters exist.");
    assertEquals(pagedModel, response.getBody(),
        "Response body content size should match the number of release letters returned.");

    verify(releaseLetterService).findAllReleaseLetters(pageable);
    verify(pagedResourcesAssembler).toModel(mockReleaseLetters, releaseLetterModelAssembler);
  }

  @Test
  void findAllReleaseLettersShouldReturnEmptyPagedModelWhenNoData() {
    PageRequest pageable = PageRequest.of(0, 20);
    Page<ReleaseLetter> emptyPage = Page.empty();

    PagedModel<ReleaseLetterModel> emptyModel = PagedModel.empty();

    when(releaseLetterService.findAllReleaseLetters(pageable)).thenReturn(emptyPage);
    when(pagedResourcesAssembler.toEmptyModel(any(), any())).thenReturn(PagedModel.empty());

    ResponseEntity<PagedModel<ReleaseLetterModel>> response =
        releaseLetterController.findAllReleaseLetters(pageable);

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Response status should be 200 OK when empty page returned.");
    assertEquals(emptyModel, response.getBody(),
        "Response body should be empty.");

    verify(pagedResourcesAssembler)
        .toEmptyModel(emptyPage, ReleaseLetterModel.class);
  }

  @Test
  void findReleaseLetterByIdShouldReturnReleaseLetter() {
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
  void findReleaseLetterBySprintShouldReturnReleaseLetter() {
    ReleaseLetter mockReleaseLetter = createReleaseLetterMock();
    ReleaseLetterModel model = new ReleaseLetterModel();

    when(releaseLetterService.findReleaseLetterBySprint(RELEASE_LETTER_SPRINT_NAME_SAMPLE)).thenReturn(mockReleaseLetter);
    when(releaseLetterModelAssembler.toModel(mockReleaseLetter)).thenReturn(model);

    var response = releaseLetterController.findReleaseLetterBySprint(RELEASE_LETTER_SPRINT_NAME_SAMPLE);

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Response status should be 200 OK when release letter is found.");
    assertEquals(model, response.getBody(),
        "Response should contain a body when release letter is found.");
  }

  @Test
  void findLatestReleaseLetterShouldReturnPagedModelWhenDataExists() {
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
  void findLatestReleaseLettersShouldReturnEmptyPagedModelWhenNoData() {
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
  void createReleaseLetterShouldReturnCreated() {
    ReleaseLetterModelRequest releaseLetterModelRequestMock = createReleaseLetterModelRequestMock();
    ReleaseLetter releaseLetterMock = createReleaseLetterMock();
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    when(releaseLetterService.createReleaseLetter(releaseLetterModelRequestMock))
        .thenReturn(releaseLetterMock);

    var response = releaseLetterController.createReleaseLetter(releaseLetterModelRequestMock);

    assertEquals(HttpStatus.CREATED, response.getStatusCode(),
        "Response status should be 201 CREATED when a new release letter is successfully created.");
    assertTrue(Objects.requireNonNull(response.getHeaders().getLocation()).toString().contains(releaseLetterMock.getId()),
        "The Location header should contain the ID of the newly created release letter.");
  }

  @Test
  void updateReleaseLetterShouldReturnUpdatedReleaseLetter() {
    String sprint = "S43";
    ReleaseLetterModelRequest releaseLetterModelRequestMock = createReleaseLetterModelRequestMock();

    ReleaseLetter releaseLetterMock = createReleaseLetterMock();
    ReleaseLetterModel model = new ReleaseLetterModel();

    when(releaseLetterService.updateReleaseLetter(sprint, releaseLetterModelRequestMock))
        .thenReturn(releaseLetterMock);
    when(releaseLetterModelAssembler.toModel(releaseLetterMock))
        .thenReturn(model);

    var response = releaseLetterController.updateReleaseLetter(sprint, releaseLetterModelRequestMock);

    assertEquals(HttpStatus.OK, response.getStatusCode(),
        "Response status should be 200 OK when a release letter is successfully updated.");
    assertTrue(response.hasBody(),
        "Response should contain a body after updating release letter.");
  }

  @Test
  void deleteReleaseLetter_shouldCallService() {
    releaseLetterController.deleteReleaseLetter(RELEASE_LETTER_SPRINT_NAME_SAMPLE);

    verify(releaseLetterService).deleteReleaseLetterBySprint(RELEASE_LETTER_SPRINT_NAME_SAMPLE);
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
