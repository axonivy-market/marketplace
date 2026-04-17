package com.axonivy.market.service.impl;

import com.axonivy.market.core.enums.ErrorCode;
import com.axonivy.market.core.exceptions.model.NotFoundException;
import com.axonivy.market.entity.ReleaseLetter;
import com.axonivy.market.exceptions.model.AlreadyExistedException;
import com.axonivy.market.exceptions.model.MarketException;
import com.axonivy.market.model.ReleaseLetterModelRequest;
import com.axonivy.market.repository.ReleaseLetterRepository;
import com.axonivy.market.service.ReleaseLetterService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.regex.Pattern;

@Log4j2
@Service
@RequiredArgsConstructor
public class ReleaseLetterServiceImpl implements ReleaseLetterService {
  private static final String GITHUB_USERNAME_REGEX = "@([\\p{Alnum}\\-]+)";
  private static final String FIRST_REGEX_CAPTURING_GROUP = "$1";
  private static final String GITHUB_MAIN_LINK = "https://github.com/";
  private static final Pattern GITHUB_USERNAME_PATTERN = Pattern.compile(GITHUB_USERNAME_REGEX,
      Pattern.UNICODE_CHARACTER_CLASS);
  private final ReleaseLetterRepository releaseLetterRepository;
  private final Sort defaultSorting = Sort.by(Sort.Direction.DESC, "createdAt");

  @Override
  public Page<ReleaseLetter> findAllReleaseLetters(Pageable pageable, boolean isReadOnly) {
    if (!isReadOnly) {
      return new PageImpl<>(releaseLetterRepository.findAll(defaultSorting));
    }

    Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), defaultSorting);
    return releaseLetterRepository.findAll(sortedPageable);
  }

  @Override
  public ReleaseLetter findReleaseLetterById(String id) {
    return releaseLetterRepository.findById(id).orElseThrow(
        () -> new NotFoundException(ErrorCode.RELEASE_LETTER_NOT_FOUND, "Not found release letter with id: " + id));
  }

  @Override
  public Page<ReleaseLetter> findLatestReleaseLetter(Pageable pageable) {
    return releaseLetterRepository.findByIsLatest(true, pageable);
  }

  @Override
  @Transactional
  public ReleaseLetter createReleaseLetter(ReleaseLetterModelRequest releaseLetterModelRequest) {
    validateReleaseLetterModelRequest(releaseLetterModelRequest);

    String unifiedSprint = unifySprint(releaseLetterModelRequest.getSprint());
    if (isSprintExisted(unifiedSprint)) {
      throw new AlreadyExistedException(ErrorCode.RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.getCode(),
          ErrorCode.RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.getHelpText());
    }
    var releaseLetter = ReleaseLetter.builder()
        .content(transformContent(releaseLetterModelRequest.getContent()))
        .draftContent(transformContent(releaseLetterModelRequest.getDraftContent()))
        .sprint(unifiedSprint)
        .isLatest(releaseLetterModelRequest.isLatest())
        .build();

    if (releaseLetterModelRequest.isLatest()) {
      releaseLetterRepository.deactivateOtherLatestReleaseLetters(unifiedSprint);
    }

    return releaseLetterRepository.save(releaseLetter);
  }

  @Override
  @Transactional
  public ReleaseLetter updateReleaseLetter(String id, ReleaseLetterModelRequest releaseLetterModelRequest) {
    validateReleaseLetterModelRequest(releaseLetterModelRequest);

    var foundReleaseLetter = findReleaseLetterById(id);
    String unifiedSelectedSprint = unifySprint(foundReleaseLetter.getSprint());
    String unifiedNewSprint = unifySprint(releaseLetterModelRequest.getSprint());

    if (!unifiedSelectedSprint.equals(unifiedNewSprint) && isSprintExisted(unifiedNewSprint)) {
      throw new AlreadyExistedException(ErrorCode.RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.getCode(),
          ErrorCode.RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.getHelpText());
    }

    foundReleaseLetter.setLatest(releaseLetterModelRequest.isLatest());
    foundReleaseLetter.setContent(transformContent(releaseLetterModelRequest.getContent()));
    foundReleaseLetter.setDraftContent(transformContent(releaseLetterModelRequest.getDraftContent()));
    foundReleaseLetter.setSprint(unifiedNewSprint);

    if (releaseLetterModelRequest.isLatest()) {
      releaseLetterRepository.deactivateOtherLatestReleaseLetters(unifiedNewSprint);
    }

    return releaseLetterRepository.save(foundReleaseLetter);
  }

  @Transactional
  @Override
  public void deleteReleaseLetterById(String id) {
    int deletedRow = releaseLetterRepository.deleteByIdReturningCount(id);
    if (deletedRow == 0) {
      throw new NotFoundException(ErrorCode.RELEASE_LETTER_NOT_FOUND, "Not found release letter with id: " + id);
    }
  }

  @Transactional
  @Override
  public ReleaseLetter saveAsDraft(String id, ReleaseLetterModelRequest releaseLetterModelRequest) {
    validateReleaseLetterModelRequest(releaseLetterModelRequest);

    ReleaseLetter releaseLetter;
    try {
      releaseLetter = findReleaseLetterById(id);
      releaseLetter = handleSavedAsDraftForExistedReleaseLetter(releaseLetter, releaseLetterModelRequest);
    } catch (NotFoundException notFoundException) {
      releaseLetter = new ReleaseLetter();
      releaseLetter = handleSavedAsDraftForNewReleaseLetter(releaseLetter, releaseLetterModelRequest);
    }

    return releaseLetterRepository.save(releaseLetter);
  }

  private void validateReleaseLetterModelRequest(ReleaseLetterModelRequest releaseLetterModelRequest) {
    if (releaseLetterModelRequest.getSprint() == null
        || ObjectUtils.isEmpty(releaseLetterModelRequest.getSprint().trim())) {
      throw new MarketException(ErrorCode.SPRINT_CANNOT_BE_BLANK.getCode(),
          ErrorCode.SPRINT_CANNOT_BE_BLANK.getHelpText());
    }
  }

  private ReleaseLetter handleSavedAsDraftForExistedReleaseLetter(ReleaseLetter foundReleaseLetter,
      ReleaseLetterModelRequest releaseLetterModelRequest) {
    String unifiedSelectedSprint = unifySprint(foundReleaseLetter.getSprint());
    String unifiedNewSprint = unifySprint(releaseLetterModelRequest.getSprint());

    if (!unifiedSelectedSprint.equals(unifiedNewSprint) && isSprintExisted(unifiedNewSprint)) {
      throw new AlreadyExistedException(ErrorCode.RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.getCode(),
          ErrorCode.RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.getHelpText());
    }

    foundReleaseLetter.setDraftContent(transformContent(releaseLetterModelRequest.getDraftContent()));
    foundReleaseLetter.setSprint(unifiedNewSprint);

    return foundReleaseLetter;
  }

  private ReleaseLetter handleSavedAsDraftForNewReleaseLetter(ReleaseLetter newReleaseLetter,
      ReleaseLetterModelRequest releaseLetterModelRequest) {
    String unifiedNewSprint = unifySprint(releaseLetterModelRequest.getSprint());

    newReleaseLetter.setDraftContent(transformContent(releaseLetterModelRequest.getDraftContent()));
    newReleaseLetter.setSprint(unifiedNewSprint);

    return newReleaseLetter;
  }

  private boolean isSprintExisted(String requestedSprint) {
    return releaseLetterRepository.existsBySprint(unifySprint(requestedSprint));
  }

  private String unifySprint(String originalInputSprint) {
    return originalInputSprint.trim().toUpperCase(Locale.getDefault());
  }

  private String transformContent(String originalContent) {
    if (ObjectUtils.isEmpty(originalContent)) {
      return "";
    }
    return GITHUB_USERNAME_PATTERN.matcher(originalContent).replaceAll(GITHUB_MAIN_LINK + FIRST_REGEX_CAPTURING_GROUP);
  }
}
