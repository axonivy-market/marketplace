package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.core.exceptions.model.NotFoundException;
import com.axonivy.market.entity.ReleaseLetter;
import com.axonivy.market.entity.ReleaseLetterDraft;
import com.axonivy.market.exceptions.model.AlreadyExistedException;
import com.axonivy.market.exceptions.model.MarketException;
import com.axonivy.market.model.ReleaseLetterDraftModel;
import com.axonivy.market.model.ReleaseLetterModelRequest;
import com.axonivy.market.repository.ReleaseLetterDraftRepository;
import com.axonivy.market.repository.ReleaseLetterRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReleaseLetterServiceImplTest extends BaseSetup {

  private static final String RELEASE_LETTER_SPRINT_NAME_SAMPLE = "demo";
  private static final String UNIFIED_RELEASE_LETTER_SPRINT_NAME = "DEMO";
  private static final String RELEASE_LETTER_CONTENT_SAMPLE = "Demo content";
  private static final String RELEASE_LETTER_ID_SAMPLE = "release-letter-id";
  private static final String GITHUB_USER_ID = "123456";

  @Mock
  private ReleaseLetterRepository releaseLetterRepository;

  @Mock
  private ReleaseLetterDraftRepository releaseLetterDraftRepository;

  @InjectMocks
  private ReleaseLetterServiceImpl releaseLetterService;

  @Test
  void testShouldUseDefaultSortingWhenNotSorted() {
    PageRequest pageable = PageRequest.of(0, 10);
    boolean isReadOnly = true;

    ReleaseLetter releaseLetterMock = createReleaseLetterMock();
    Page<ReleaseLetter> page = new PageImpl<>(List.of(releaseLetterMock));

    when(releaseLetterRepository.findAllWithContent(any(Pageable.class))).thenReturn(page);

    Page<ReleaseLetter> result = releaseLetterService.findAllReleaseLetters(pageable, isReadOnly);

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);

    verify(releaseLetterRepository).findAllWithContent(captor.capture());

    Pageable usedPageable = captor.getValue();

    assertNotNull(usedPageable.getSort().getOrderFor("createdAt"),
        "Sort order should not be null");
    assertEquals(Sort.Direction.DESC,
        Objects.requireNonNull(usedPageable.getSort().getOrderFor("createdAt")).getDirection(),
        "Sort order should be createdAt DESC");
    assertEquals(1, result.getTotalElements(), "Total elements in page should be 1");
    assertEquals(1, result.getContent().size(), "Content list size should be 1");
  }

  @Test
  void testShouldReturnReleaseLetterWhenIdExists() {
    ReleaseLetter releaseLetterMock = createReleaseLetterMock();
    when(releaseLetterRepository.findById(RELEASE_LETTER_ID_SAMPLE))
        .thenReturn(Optional.of(releaseLetterMock));

    ReleaseLetter result = releaseLetterService.findReleaseLetterById(RELEASE_LETTER_ID_SAMPLE);

    assertEquals(RELEASE_LETTER_ID_SAMPLE, result.getId(),
        "Id of found release letter should match requested id");
  }

  @Test
  void testShouldThrowNotFoundExceptionWhenIdNotExists() {
    when(releaseLetterRepository.findById(RELEASE_LETTER_ID_SAMPLE))
        .thenReturn(Optional.empty());

    assertThrows(NotFoundException.class,
        () -> releaseLetterService.findReleaseLetterById(RELEASE_LETTER_ID_SAMPLE),
        "Expected NotFoundException to be thrown when id does not exist");
  }

  @Test
  void testShouldReturnLatestReleaseLetters() {
    PageRequest pageable = PageRequest.of(0, 20);
    ReleaseLetter releaseLetterMock = createReleaseLetterMock();
    Page<ReleaseLetter> page = new PageImpl<>(List.of(releaseLetterMock));

    when(releaseLetterRepository.findByIsLatest(true, pageable)).thenReturn(page);

    Page<ReleaseLetter> result = releaseLetterService.findLatestReleaseLetter(pageable);

    verify(releaseLetterRepository).findByIsLatest(true, pageable);

    assertEquals(page, result, "Resulting page of latest ReleaseLetters should match repository response");
  }

  @Test
  void testShouldThrowMarketExceptionWhenSprintNameIsEmpty() {
    ReleaseLetterModelRequest releaseLetterModelRequestMock = new ReleaseLetterModelRequest();
    releaseLetterModelRequestMock.setSprint("   ");

    assertThrows(MarketException.class,
        () -> releaseLetterService.createReleaseLetter(releaseLetterModelRequestMock, false),
        "Expected MarketException to be thrown when sprint name is blank");
  }

  @Test
  void testShouldThrowMarketExceptionWhenSprintNameIsNull() {
    ReleaseLetterModelRequest releaseLetterModelRequestMock = new ReleaseLetterModelRequest();
    releaseLetterModelRequestMock.setSprint(null);

    assertThrows(MarketException.class,
        () -> releaseLetterService.createReleaseLetter(releaseLetterModelRequestMock, false),
        "Expected MarketException to be thrown when sprint name is blank");
  }

  @Test
  void testShouldThrowWhenSprintAlreadyExists() {
    ReleaseLetterModelRequest request = new ReleaseLetterModelRequest();
    request.setSprint("S43");

    when(releaseLetterRepository.existsBySprint("S43"))
        .thenReturn(true);
    assertThrows(AlreadyExistedException.class,
        () -> releaseLetterService.createReleaseLetter(request, false),
        "Expected AlreadyExistedException to be thrown when sprint name already exists");
  }

  @Test
  void testShouldCreateReleaseLetterSuccessfully() {
    ReleaseLetterModelRequest request = new ReleaseLetterModelRequest();
    request.setSprint(RELEASE_LETTER_SPRINT_NAME_SAMPLE);
    request.setContent("Thanks @john");
    request.setLatest(false);

    when(releaseLetterRepository.existsBySprint(RELEASE_LETTER_SPRINT_NAME_SAMPLE.trim().toUpperCase())).thenReturn(
        false);
    when(releaseLetterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    ReleaseLetter result = releaseLetterService.createReleaseLetter(request, false);

    assertEquals(UNIFIED_RELEASE_LETTER_SPRINT_NAME, result.getSprint(),
        "Result sprint name should be unified to uppercase");
    assertEquals("Thanks https://github.com/john", result.getContent(),
        "Content should transform GitHub username into GitHub profile link");
  }

  @Test
  void testCreateReleaseLetterShouldSetEmptyContentWhenContentIsNull() {
    ReleaseLetterModelRequest request = new ReleaseLetterModelRequest();
    request.setId(RELEASE_LETTER_ID_SAMPLE);
    request.setSprint("S45");
    request.setContent(null);
    request.setLatest(false);

    when(releaseLetterRepository.existsBySprint("S45")).thenReturn(false);
    when(releaseLetterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    ReleaseLetter result = releaseLetterService.createReleaseLetter(request, false);

    assertNotNull(result, "Result should not be null");
    assertEquals("", result.getContent(),
        "Content should be empty string when original content is null");
  }

  @Test
  void testShouldDeactivateOthersWhenLatestIsTrue() {
    ReleaseLetterModelRequest request = new ReleaseLetterModelRequest();
    request.setSprint("S43");
    request.setContent("content");
    request.setLatest(true);

    when(releaseLetterRepository.existsBySprint("S43"))
        .thenReturn(false);

    releaseLetterService.createReleaseLetter(request, false);

    verify(releaseLetterRepository).deactivateOtherLatestReleaseLetters("S43");
  }

  @Test
  void testShouldUpdateSuccessfully() {
    ReleaseLetter releaseLetterMock = createReleaseLetterMock();
    ReleaseLetterModelRequest request = new ReleaseLetterModelRequest();
    request.setId(RELEASE_LETTER_ID_SAMPLE);
    request.setSprint("S44");
    request.setContent("Hello @dev");
    request.setLatest(true);

    when(releaseLetterRepository.findById(request.getId())).thenReturn(Optional.of(releaseLetterMock));
    when(releaseLetterRepository.existsBySprint("S44")).thenReturn(false);
    when(releaseLetterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    ReleaseLetter result = releaseLetterService.updateReleaseLetter(request.getId(), request, GITHUB_USER_ID);

    assertEquals("S44", result.getSprint(), "Result sprint should match requested Sprint");
    assertEquals("Hello https://github.com/dev", result.getContent(),
        "Result content should have the correct transformed github account link");

    verify(releaseLetterRepository).deactivateOtherLatestReleaseLetters("S44");
    verify(releaseLetterDraftRepository).deleteByGitHubUserIdAndReleaseLetterId(
        GITHUB_USER_ID,
        RELEASE_LETTER_ID_SAMPLE
    );
  }

  @Test
  void testUpdateReleaseLetterShouldThrowExceptionWhenSprintIsBlank() {
    ReleaseLetterModelRequest request = new ReleaseLetterModelRequest();
    request.setSprint("   ");

    assertThrows(MarketException.class,
        () -> releaseLetterService.updateReleaseLetter(RELEASE_LETTER_ID_SAMPLE, request, GITHUB_USER_ID),
        "Expected MarketException to be thrown when sprint name is blank");

    verifyNoInteractions(releaseLetterRepository);
  }

  @Test
  void testUpdateReleaseLetterShouldThrowAlreadyExistedWhenSprintChangedAndExists() {
    ReleaseLetter releaseLetterMock = createReleaseLetterMock();
    ReleaseLetterModelRequest request = new ReleaseLetterModelRequest();
    request.setId(RELEASE_LETTER_ID_SAMPLE);
    request.setSprint("S44");

    when(releaseLetterRepository.findById(request.getId())).thenReturn(Optional.of(releaseLetterMock));
    when(releaseLetterRepository.existsBySprint("S44")).thenReturn(true);

    String id = request.getId();
    assertThrows(AlreadyExistedException.class,
        () -> releaseLetterService.updateReleaseLetter(id, request, GITHUB_USER_ID),
        "Expected AlreadyExistedException to be thrown when sprint name already exists");
  }

  @Test
  void testUpdateReleaseLetterShouldUpdateSuccessfullyWhenSprintNotChanged() {
    ReleaseLetterModelRequest request = new ReleaseLetterModelRequest();
    request.setId(RELEASE_LETTER_ID_SAMPLE);
    request.setSprint("S43");
    request.setLatest(false);
    request.setContent("Fixed by @john");

    ReleaseLetter existing = new ReleaseLetter();
    existing.setId(RELEASE_LETTER_ID_SAMPLE);
    existing.setSprint("S43");

    when(releaseLetterRepository.findById(request.getId())).thenReturn(Optional.of(existing));
    when(releaseLetterRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    ReleaseLetter result = releaseLetterService.updateReleaseLetter(request.getId(), request, GITHUB_USER_ID);

    assertEquals("S43", result.getSprint(), "Result sprint should match requested Sprint");
    assertFalse(result.isLatest(), "Result release letter should not be active");
    assertEquals("Fixed by https://github.com/john", result.getContent(),
        "Content should transform GitHub username into GitHub profile link");

    verify(releaseLetterRepository).findById(request.getId());
    verify(releaseLetterRepository).save(existing);
    verify(releaseLetterDraftRepository).deleteByGitHubUserIdAndReleaseLetterId(
        GITHUB_USER_ID,
        RELEASE_LETTER_ID_SAMPLE
    );
  }

  @Test
  void testUpdateReleaseLetterShouldUpdateSprintWhenSprintChangedAndNotExists() {
    ReleaseLetterModelRequest request = new ReleaseLetterModelRequest();
    request.setId(RELEASE_LETTER_ID_SAMPLE);
    request.setSprint("s44");
    request.setLatest(false);
    request.setContent("Reviewed by @alice");

    ReleaseLetter existing = new ReleaseLetter();
    existing.setId(RELEASE_LETTER_ID_SAMPLE);
    existing.setSprint("S43");

    when(releaseLetterRepository.existsBySprint("S44")).thenReturn(false);
    when(releaseLetterRepository.findById(request.getId())).thenReturn(Optional.of(existing));
    when(releaseLetterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    ReleaseLetter result = releaseLetterService.updateReleaseLetter(request.getId(), request, GITHUB_USER_ID);

    assertNotNull(result, "Result should not be null when sprint is successfully changed");
    assertEquals("S44", result.getSprint(), "Sprint should be normalized to uppercase and updated");
    assertEquals("Reviewed by https://github.com/alice", result.getContent(),
        "Content should correctly replace GitHub username with profile link"
    );

    assertFalse(result.isLatest(), "Latest flag should remain false");

    verify(releaseLetterRepository).existsBySprint("S44");
    verify(releaseLetterRepository).save(existing);
    verify(releaseLetterDraftRepository).deleteByGitHubUserIdAndReleaseLetterId(
        GITHUB_USER_ID,
        RELEASE_LETTER_ID_SAMPLE
    );
  }

  @Test
  void testUpdateReleaseLetterShouldDeactivateOthersWhenIsLatestTrue() {
    ReleaseLetterModelRequest request = new ReleaseLetterModelRequest();
    request.setId(RELEASE_LETTER_ID_SAMPLE);
    request.setSprint("S43");
    request.setLatest(true);
    request.setContent("Thanks @bob");

    ReleaseLetter existing = new ReleaseLetter();
    existing.setId(RELEASE_LETTER_ID_SAMPLE);
    existing.setSprint("S43");

    when(releaseLetterRepository.findById(request.getId())).thenReturn(Optional.of(existing));
    when(releaseLetterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    ReleaseLetter result = releaseLetterService.updateReleaseLetter(request.getId(), request, GITHUB_USER_ID);

    assertNotNull(result, "Result should not be null when marking release letter as latest");
    assertTrue(result.isLatest(), "Latest flag should be updated to true");

    assertEquals("Thanks https://github.com/bob", result.getContent(),
        "Content should correctly transform GitHub username into link"
    );

    verify(releaseLetterRepository).deactivateOtherLatestReleaseLetters("S43");
    verify(releaseLetterRepository).save(existing);
  }

  @Test
  void testDeleteReleaseLetterWhenIdExists() {
    String id = "123";

    when(releaseLetterRepository.deleteByIdReturningCount(id)).thenReturn(1);

    releaseLetterService.deleteReleaseLetterById(id);
    verify(releaseLetterRepository).deleteByIdReturningCount(id);
    verify(releaseLetterDraftRepository).deleteByReleaseLetterId(id);
  }

  @Test
  void testDeleteReleaseLetterByIdShouldThrowNotFoundExceptionWhenIdDoesNotExist() {
    String id = "123";

    when(releaseLetterRepository.deleteByIdReturningCount(id)).thenReturn(0);
    assertThrows(NotFoundException.class,
        () -> releaseLetterService.deleteReleaseLetterById(id),
        "Expected NotFoundException to be thrown when id does not exist");

    verify(releaseLetterRepository).deleteByIdReturningCount(id);
  }

  @Test
  void testFindAllReleaseLettersShouldReturnSinglePageWhenPagingDisabled() {
    Pageable pageable = PageRequest.of(5, 1);
    boolean isReadOnly = false;

    ReleaseLetter releaseLetterMock = createReleaseLetterMock();
    List<ReleaseLetter> list = List.of(releaseLetterMock);

    when(releaseLetterRepository.findAll(any(Sort.class))).thenReturn(list);

    Page<ReleaseLetter> result = releaseLetterService.findAllReleaseLetters(pageable, isReadOnly);
    ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);

    verify(releaseLetterRepository).findAll(sortCaptor.capture());

    Sort usedSort = sortCaptor.getValue();
    Sort.Order order = usedSort.getOrderFor("createdAt");

    assertNotNull(order, "Sort should contain 'createdAt' property");
    assertEquals(Sort.Direction.DESC, order.getDirection(), "Sort direction should be DESC");
    assertEquals(0, result.getNumber(), "Page number should always be 0 when paging disabled");
    assertEquals(1, result.getSize(), "Page size should equal list size");
    assertEquals(1, result.getTotalElements(), "Total elements should equal list size");
    assertEquals(list, result.getContent(), "Page content should match repository result");
  }

  @Test
  void testSaveAsDraftShouldCreateReleaseLetterWhenReleaseLetterDoesNotExist() {
    ReleaseLetterModelRequest request = new ReleaseLetterModelRequest();
    request.setId(RELEASE_LETTER_ID_SAMPLE);
    request.setSprint("s45");
    request.setDraftContent("Draft by @john");

    ReleaseLetter createdReleaseLetter = new ReleaseLetter();
    createdReleaseLetter.setId(RELEASE_LETTER_ID_SAMPLE);
    createdReleaseLetter.setSprint("S45");

    when(releaseLetterRepository.findById(RELEASE_LETTER_ID_SAMPLE)).thenReturn(Optional.empty());
    when(releaseLetterRepository.existsBySprint("S45")).thenReturn(false);
    when(releaseLetterRepository.save(any(ReleaseLetter.class))).thenReturn(createdReleaseLetter);
    when(releaseLetterDraftRepository.findByGitHubUserIdAndReleaseLetterId(
        GITHUB_USER_ID,
        RELEASE_LETTER_ID_SAMPLE
    )).thenReturn(Optional.empty());
    when(releaseLetterDraftRepository.save(any(ReleaseLetterDraft.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ReleaseLetterDraftModel result = releaseLetterService.saveAsDraft(request, GITHUB_USER_ID);

    assertNotNull(result, "Saved draft result should not be null");
    assertEquals(RELEASE_LETTER_ID_SAMPLE, result.getReleaseLetterId(),
        "Release letter id should match created release letter");
    assertEquals("Draft by https://github.com/john", result.getDraftContent(),
        "Draft content should transform GitHub username into profile link");

    verify(releaseLetterRepository).findById(RELEASE_LETTER_ID_SAMPLE);
    verify(releaseLetterRepository).save(any(ReleaseLetter.class));
    verify(releaseLetterDraftRepository).save(any(ReleaseLetterDraft.class));
  }

  @Test
  void testSaveAsDraftShouldUpdateExistingDraftWhenDraftAlreadyExists() {
    ReleaseLetterModelRequest request = new ReleaseLetterModelRequest();
    request.setId(RELEASE_LETTER_ID_SAMPLE);
    request.setSprint("S43");
    request.setDraftContent("Updated by @alice");

    ReleaseLetter existingReleaseLetter = createReleaseLetterMock();

    ReleaseLetterDraft existingDraft = new ReleaseLetterDraft();
    existingDraft.setId("draft-id");
    existingDraft.setReleaseLetterId(RELEASE_LETTER_ID_SAMPLE);
    existingDraft.setGitHubUserId(GITHUB_USER_ID);
    existingDraft.setDraftContent("Old draft");

    when(releaseLetterRepository.findById(RELEASE_LETTER_ID_SAMPLE))
        .thenReturn(Optional.of(existingReleaseLetter));
    when(releaseLetterDraftRepository.findByGitHubUserIdAndReleaseLetterId(
        GITHUB_USER_ID,
        RELEASE_LETTER_ID_SAMPLE
    )).thenReturn(Optional.of(existingDraft));
    when(releaseLetterDraftRepository.save(any(ReleaseLetterDraft.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ReleaseLetterDraftModel result =
        releaseLetterService.saveAsDraft(request, GITHUB_USER_ID);

    assertNotNull(result,
        "Updated draft result should not be null");
    assertEquals(RELEASE_LETTER_ID_SAMPLE, result.getReleaseLetterId(),
        "Release letter id should remain unchanged");
    assertEquals("Updated by https://github.com/alice", result.getDraftContent(),
        "Draft content should be updated and transformed correctly");
    assertEquals("draft-id", result.getId(),
        "Draft id should remain unchanged after update");
    verify(releaseLetterRepository).findById(RELEASE_LETTER_ID_SAMPLE);
    verify(releaseLetterDraftRepository).save(existingDraft);
  }

  @Test
  void testSaveAsDraftShouldThrowMarketExceptionWhenSprintIsBlank() {
    ReleaseLetterModelRequest request = new ReleaseLetterModelRequest();
    request.setSprint("   ");

    assertThrows(MarketException.class,
        () -> releaseLetterService.saveAsDraft(request, GITHUB_USER_ID),
        "Expected MarketException to be thrown when sprint is blank");

    verifyNoInteractions(releaseLetterRepository);
    verifyNoInteractions(releaseLetterDraftRepository);
  }

  @Test
  void testGetDraftContentByGitHubUserIdAndReleaseLetterIdShouldReturnDraftWhenExists() {
    ReleaseLetterDraft existingDraft = new ReleaseLetterDraft();
    existingDraft.setReleaseLetterId(RELEASE_LETTER_ID_SAMPLE);
    existingDraft.setGitHubUserId(GITHUB_USER_ID);
    existingDraft.setDraftContent("Draft content");

    when(releaseLetterDraftRepository.findByGitHubUserIdAndReleaseLetterId(
        GITHUB_USER_ID,
        RELEASE_LETTER_ID_SAMPLE
    )).thenReturn(Optional.of(existingDraft));

    ReleaseLetterDraft result =
        releaseLetterService.getDraftContentByGitHubUserIdAndReleaseLetterId(
            GITHUB_USER_ID,
            RELEASE_LETTER_ID_SAMPLE
        );

    assertNotNull(result, "Returned draft should not be null");
    assertEquals("Draft content", result.getDraftContent(),
        "Draft content should match repository result");

    verify(releaseLetterDraftRepository)
        .findByGitHubUserIdAndReleaseLetterId(GITHUB_USER_ID, RELEASE_LETTER_ID_SAMPLE);
  }

  @Test
  void testGetDraftContentByGitHubUserIdAndReleaseLetterIdShouldReturnNullWhenDraftDoesNotExist() {
    when(releaseLetterDraftRepository.findByGitHubUserIdAndReleaseLetterId(
        GITHUB_USER_ID,
        RELEASE_LETTER_ID_SAMPLE
    )).thenReturn(Optional.empty());

    ReleaseLetterDraft result =
        releaseLetterService.getDraftContentByGitHubUserIdAndReleaseLetterId(
            GITHUB_USER_ID,
            RELEASE_LETTER_ID_SAMPLE
        );

    assertNull(result, "Result should be null when draft does not exist");

    verify(releaseLetterDraftRepository)
        .findByGitHubUserIdAndReleaseLetterId(GITHUB_USER_ID, RELEASE_LETTER_ID_SAMPLE);
  }

  @Test
  void testSaveAsDraftShouldSetEmptyDraftContentWhenDraftContentIsNull() {
    ReleaseLetterModelRequest request = new ReleaseLetterModelRequest();
    request.setId(RELEASE_LETTER_ID_SAMPLE);
    request.setSprint("S43");
    request.setDraftContent(null);

    ReleaseLetter existingReleaseLetter = createReleaseLetterMock();

    when(releaseLetterRepository.findById(RELEASE_LETTER_ID_SAMPLE))
        .thenReturn(Optional.of(existingReleaseLetter));
    when(releaseLetterDraftRepository.findByGitHubUserIdAndReleaseLetterId(
        GITHUB_USER_ID,
        RELEASE_LETTER_ID_SAMPLE
    )).thenReturn(Optional.empty());
    when(releaseLetterDraftRepository.save(any(ReleaseLetterDraft.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ReleaseLetterDraftModel result =
        releaseLetterService.saveAsDraft(request, GITHUB_USER_ID);

    assertNotNull(result,
        "Saved draft result should not be null");
    assertEquals("", result.getDraftContent(),
        "Draft content should be empty string when original draft content is null");
  }

  private ReleaseLetter createReleaseLetterMock() {
    ReleaseLetter mockReleaseLetter = new ReleaseLetter();
    mockReleaseLetter.setSprint(UNIFIED_RELEASE_LETTER_SPRINT_NAME);
    mockReleaseLetter.setContent(RELEASE_LETTER_CONTENT_SAMPLE);
    mockReleaseLetter.setId(RELEASE_LETTER_ID_SAMPLE);
    mockReleaseLetter.setLatest(true);

    return mockReleaseLetter;
  }
}
