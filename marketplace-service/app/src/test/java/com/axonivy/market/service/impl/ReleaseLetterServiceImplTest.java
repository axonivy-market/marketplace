package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.core.exceptions.model.NotFoundException;
import com.axonivy.market.entity.Feedback;
import com.axonivy.market.entity.ReleaseLetter;
import com.axonivy.market.enums.FeedbackStatus;
import com.axonivy.market.exceptions.model.AlreadyExistedException;
import com.axonivy.market.exceptions.model.MarketException;
import com.axonivy.market.model.FeedbackModel;
import com.axonivy.market.model.FeedbackModelRequest;
import com.axonivy.market.model.ReleaseLetterModelRequest;
import com.axonivy.market.repository.ReleaseLetterRepository;
import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;

import static org.mockito.ArgumentMatchers.any;

import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ReleaseLetterServiceImplTest extends BaseSetup {

  private static final String RELEASE_LETTER_SPRINT_NAME_SAMPLE = "demo";
  private static final String UNIFIED_RELEASE_LETTER_SPRINT_NAME = "DEMO";
  private static final String RELEASE_LETTER_CONTENT_SAMPLE = "Demo content";
  private static final String RELEASE_LETTER_ID_SAMPLE = "release-letter-id";

  @Mock
  private ReleaseLetterRepository releaseLetterRepository;

  @InjectMocks
  private ReleaseLetterServiceImpl releaseLetterService;

  private ReleaseLetter releaseLetter;

  private ReleaseLetterModelRequest releaseLetterModelRequest;

  @BeforeEach
  void setUp() {
    releaseLetter = new ReleaseLetter();
    releaseLetter.setSprint(RELEASE_LETTER_SPRINT_NAME_SAMPLE);
    releaseLetter.setContent(RELEASE_LETTER_CONTENT_SAMPLE);
    releaseLetter.setId(RELEASE_LETTER_ID_SAMPLE);
    releaseLetter.setLatest(true);

    releaseLetterModelRequest = new ReleaseLetterModelRequest();
    releaseLetterModelRequest.setSprint(RELEASE_LETTER_SPRINT_NAME_SAMPLE);
    releaseLetterModelRequest.setContent(RELEASE_LETTER_CONTENT_SAMPLE);
    releaseLetterModelRequest.setLatest(true);
  }

  @Test
  void shouldUseDefaultSortingWhenNotSorted() {
    PageRequest pageable = PageRequest.of(0, 10);
    Page<ReleaseLetter> page = new PageImpl<>(List.of(releaseLetter));

    when(releaseLetterRepository.findAll(any(Pageable.class)))
        .thenReturn(page);

    Page<ReleaseLetter> result = releaseLetterService.findAllReleaseLetters(pageable);

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(releaseLetterRepository).findAll(captor.capture());

    Pageable usedPageable = captor.getValue();

    assertNotNull(usedPageable.getSort().getOrderFor("createdAt"),
        "Sort order should not be null");
    assertEquals(Sort.Direction.DESC,
        Objects.requireNonNull(usedPageable.getSort().getOrderFor("createdAt")).getDirection(),
        "Sort order should be createAt");
    assertEquals(1, result.getTotalElements(), "Total elements in page should be 1");
    assertEquals(1, result.getContent().size(), "Content list size should be 1");
  }

  @Test
  void shouldUseGivenSortingWhenAlreadySorted() {
    Pageable pageable = PageRequest.of(0, 10, Sort.by("sprint"));
    Page<ReleaseLetter> page = new PageImpl<>(List.of(releaseLetter));

    when(releaseLetterRepository.findAll(pageable)).thenReturn(page);

    Page<ReleaseLetter> result = releaseLetterService.findAllReleaseLetters(pageable);

    verify(releaseLetterRepository).findAll(pageable);
    assertEquals(1, result.getTotalElements(), "Total elements in page should be 1");
    assertEquals(1, result.getContent().size(), "Content list size should be 1");
  }

  @Test
  void shouldReturnReleaseLetterWhenIdExists() {
    when(releaseLetterRepository.findById(RELEASE_LETTER_ID_SAMPLE))
        .thenReturn(Optional.of(releaseLetter));

    ReleaseLetter result = releaseLetterService.findReleaseLetterById(RELEASE_LETTER_ID_SAMPLE);

    assertEquals(RELEASE_LETTER_ID_SAMPLE, result.getId(),
        "Id of found release letter should match requested id");
  }

  @Test
  void shouldThrowNotFoundExceptionWhenIdNotExists() {
    when(releaseLetterRepository.findById(RELEASE_LETTER_ID_SAMPLE))
        .thenReturn(Optional.empty());

    assertThrows(NotFoundException.class,
        () -> releaseLetterService.findReleaseLetterById(RELEASE_LETTER_ID_SAMPLE),
        "Expected NotFoundException to be thrown when id does not exist");
  }

  @Test
  void shouldReturnReleaseLetterWhenSprintNameExists() {
    when(releaseLetterRepository.findBySprint(UNIFIED_RELEASE_LETTER_SPRINT_NAME))
        .thenReturn(Optional.of(releaseLetter));

    ReleaseLetter result = releaseLetterService.findReleaseLetterBySprint(RELEASE_LETTER_SPRINT_NAME_SAMPLE);

    assertEquals(RELEASE_LETTER_SPRINT_NAME_SAMPLE, result.getSprint(),
        "Id of found release letter should match requested id");
  }

  @Test
  void shouldThrowNotFoundExceptionWhenSprintNameNotExists() {
    when(releaseLetterRepository.findBySprint(UNIFIED_RELEASE_LETTER_SPRINT_NAME))
        .thenReturn(Optional.empty());

    assertThrows(NotFoundException.class,
        () -> releaseLetterService.findReleaseLetterBySprint(RELEASE_LETTER_SPRINT_NAME_SAMPLE),
        "Expected NotFoundException to be thrown when sprint name does not exist");
  }
  @Test
  void shouldReturnLatestReleaseLetters() {
    PageRequest pageable = PageRequest.of(0, 20);
    Page<ReleaseLetter> page = new PageImpl<>(List.of(releaseLetter));

    when(releaseLetterRepository.findByIsLatest(true, pageable)).thenReturn(page);

    Page<ReleaseLetter> result = releaseLetterService.findLatestReleaseLetter(pageable);

    verify(releaseLetterRepository).findByIsLatest(true, pageable);

    assertEquals(page, result);
  }

  @Test
  void shouldThrowMarketExceptionWhenSprintNameIsEmpty() {
    ReleaseLetterModelRequest releaseLetterModelRequestMock = new ReleaseLetterModelRequest();
    releaseLetterModelRequestMock.setSprint("   ");

    assertThrows(MarketException.class,
        () -> releaseLetterService.createReleaseLetter(releaseLetterModelRequestMock),
        "Expected MarketException to be thrown when sprint name is blank");
  }

  @Test
  void shouldThrowWhenSprintAlreadyExists() {
    ReleaseLetterModelRequest request = new ReleaseLetterModelRequest();
    request.setSprint("S43");

    when(releaseLetterRepository.existsBySprint("S43"))
        .thenReturn(true);
    assertThrows(AlreadyExistedException.class,
        () -> releaseLetterService.createReleaseLetter(request),
        "Expected AlreadyExistedException to be thrown when sprint name already exists");
  }

  @Test
  void shouldCreateReleaseLetterSuccessfully() {
    ReleaseLetterModelRequest request = new ReleaseLetterModelRequest();
    request.setSprint(RELEASE_LETTER_SPRINT_NAME_SAMPLE);
    request.setContent("Thanks @john");
    request.setLatest(false);

    when(releaseLetterRepository.existsBySprint(RELEASE_LETTER_SPRINT_NAME_SAMPLE.trim().toUpperCase())).thenReturn(false);
    when(releaseLetterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    ReleaseLetter result = releaseLetterService.createReleaseLetter(request);

    assertEquals(RELEASE_LETTER_SPRINT_NAME_SAMPLE.trim().toUpperCase(), result.getSprint());
    assertEquals("Thanks https://github.com/john", result.getContent());
  }


//  @Test
//  void shouldDeactivateOthersWhenLatestIsTrue() {
//    ReleaseLetterModelRequest request = new ReleaseLetterModelRequest();
//    request.setSprint("S43");
//    request.setContent("content");
//    request.setLatest(true);
//
//    when(releaseLetterRepository.existsBySprint("S43"))
//        .thenReturn(false);
//
//    service.createReleaseLetter(request);
//
//    verify(releaseLetterRepository)
//        .deactivateOtherLatestReleaseLetters("S43");
//  }

//  @Test
//  void shouldUnifySprintAndReturnReleaseLetter() {
//    when(releaseLetterRepository.findBySprint(RELEASE_LETTER_SPRINT_NAME_SAMPLE))
//        .thenReturn(Optional.of(releaseLetter));
//
//    ReleaseLetter result = releaseLetterService.findReleaseLetterBySprint(RELEASE_LETTER_SPRINT_NAME_SAMPLE);
//
//    assertEquals(releaseLetter, result);
////    assertThat(result).isEqualTo(releaseLetter);
//  }

  private ReleaseLetter createReleaseLetterMock() {
    ReleaseLetter mockReleaseLetter = new ReleaseLetter();
    mockReleaseLetter.setSprint(RELEASE_LETTER_SPRINT_NAME_SAMPLE);
    mockReleaseLetter.setContent(RELEASE_LETTER_CONTENT_SAMPLE);
    mockReleaseLetter.setId(RELEASE_LETTER_ID_SAMPLE);
    mockReleaseLetter.setLatest(true);

    return mockReleaseLetter;
  }
}
