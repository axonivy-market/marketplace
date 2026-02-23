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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

  @Override
  public Page<ReleaseLetter> findAllReleaseLetters(Pageable pageable) {
    Pageable sortedPageable =
        pageable.getSort().isSorted()
            ? pageable
            : PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.by(Sort.Direction.DESC, "createdAt")
        );

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
  public ReleaseLetter findReleaseLetterBySprint(String sprint) {
    return releaseLetterRepository.findBySprint(unifySprint(sprint)).orElseThrow(
        () -> new NotFoundException(ErrorCode.RELEASE_LETTER_NOT_FOUND,
            "Not found release letter of sprint: " + sprint));
  }

  @Override
  @Transactional
  public ReleaseLetter createReleaseLetter(ReleaseLetterModelRequest releaseLetterModelRequest) {
    if (ObjectUtils.isEmpty(releaseLetterModelRequest.getSprint().trim())) {
      throw new MarketException(ErrorCode.SPRINT_CANNOT_BE_BLANK.getCode(),
          ErrorCode.SPRINT_CANNOT_BE_BLANK.getHelpText());
    }

    String unifiedSprint = unifySprint(releaseLetterModelRequest.getSprint());
    if (isSprintExisted(unifiedSprint)) {
      throw new AlreadyExistedException(ErrorCode.RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.getCode(),
          ErrorCode.RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.getHelpText());
    }
    ReleaseLetter releaseLetter =
        ReleaseLetter.builder().content(transformContent(releaseLetterModelRequest.getContent())).sprint(
            unifiedSprint).isLatest(releaseLetterModelRequest.isLatest()).build();

    if (releaseLetterModelRequest.isLatest()) {
      releaseLetterRepository.deactivateOtherLatestReleaseLetters(unifiedSprint);
    }

    return releaseLetterRepository.save(releaseLetter);
  }

  @Override
  public ReleaseLetter updateReleaseLetter(String selectedSprint, ReleaseLetterModelRequest releaseLetterModelRequest) {
    if (ObjectUtils.isEmpty(releaseLetterModelRequest.getSprint().trim())) {
      throw new MarketException(ErrorCode.SPRINT_CANNOT_BE_BLANK.getCode(),
          ErrorCode.SPRINT_CANNOT_BE_BLANK.getHelpText());
    }
    String unifiedSelectedSprint = unifySprint(selectedSprint);
    String unifiedNewSprint = unifySprint(releaseLetterModelRequest.getSprint());

    if (!unifiedSelectedSprint.equals(unifiedNewSprint) && isSprintExisted(
        unifiedNewSprint)) {
      throw new AlreadyExistedException(ErrorCode.RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.getCode(),
          ErrorCode.RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.getHelpText());
    }

    var foundReleaseLetter = findReleaseLetterBySprint(selectedSprint);
    foundReleaseLetter.setLatest(releaseLetterModelRequest.isLatest());
    foundReleaseLetter.setContent(transformContent(releaseLetterModelRequest.getContent()));
    foundReleaseLetter.setSprint(unifiedNewSprint);

    if (releaseLetterModelRequest.isLatest()) {
      releaseLetterRepository.deactivateOtherLatestReleaseLetters(unifiedNewSprint);
    }

    return releaseLetterRepository.save(foundReleaseLetter);
  }

  @Transactional
  @Override
  public void deleteReleaseLetterBySprint(String sprint) {
    String unifiedSelectedSprint = unifySprint(sprint);
    findReleaseLetterBySprint(unifiedSelectedSprint);
    releaseLetterRepository.deleteBySprint(sprint);
  }

  private boolean isSprintExisted(String requestedSprint) {
    return releaseLetterRepository.existsBySprint(unifySprint(requestedSprint));
  }

  private String unifySprint(String originalInputSprint) {
    return originalInputSprint.trim().toUpperCase();
  }

  private String transformContent(String originalContent) {
    return GITHUB_USERNAME_PATTERN.matcher(originalContent).replaceAll(
        GITHUB_MAIN_LINK + FIRST_REGEX_CAPTURING_GROUP);
  }
}
