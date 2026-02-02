package com.axonivy.market.service.impl;

import com.axonivy.market.core.enums.ErrorCode;
import com.axonivy.market.core.exceptions.model.NotFoundException;
import com.axonivy.market.entity.ReleaseLetter;
import com.axonivy.market.exceptions.model.AlreadyExistedException;
import com.axonivy.market.model.ReleaseLetterModelRequest;
import com.axonivy.market.repository.ReleaseLetterRepository;
import com.axonivy.market.service.ReleaseLetterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class ReleaseLetterServiceImpl implements ReleaseLetterService {
  private final ReleaseLetterRepository releaseLetterRepository;

  @Override
  public Page<ReleaseLetter> findAllReleaseLetters(Pageable pageable) {
    return releaseLetterRepository.findAll(pageable);
  }

  @Override
  public ReleaseLetter findReleaseLetterById(String id) {
    return releaseLetterRepository.findById(id).orElseThrow(
        () -> new NotFoundException(ErrorCode.RELEASE_LETTER_NOT_FOUND, "Not found release letter with id: " + id));
  }

  @Override
  public ReleaseLetter findReleaseLetterByReleaseVersion(String releaseVersion) {
    return releaseLetterRepository.findByReleaseVersion(unifyReleaseVersion(releaseVersion)).orElseThrow(
        () -> new NotFoundException(ErrorCode.RELEASE_LETTER_NOT_FOUND,
            "Not found release letter with release version: " + releaseVersion));
  }

  @Override
  public ReleaseLetter createReleaseLetter(ReleaseLetterModelRequest releaseLetterModelRequest) {
    String unifiedReleaseVersion = unifyReleaseVersion(releaseLetterModelRequest.getReleaseVersion());
    if (isReleaseVersionExisted(unifiedReleaseVersion)) {
      throw new AlreadyExistedException(ErrorCode.RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.getCode(),
          ErrorCode.RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.getHelpText());
    }

    ReleaseLetter releaseLetter =
        ReleaseLetter.builder().content(releaseLetterModelRequest.getContent()).releaseVersion(
            unifiedReleaseVersion).build();

    return releaseLetterRepository.save(releaseLetter);
  }

  @Override
  public ReleaseLetter updateReleaseLetter(String currentReleaseVersion, ReleaseLetterModelRequest releaseLetterModelRequest) {
    String unifiedCurrentReleaseVersion = unifyReleaseVersion(currentReleaseVersion);
    var foundReleaseLetter = findReleaseLetterByReleaseVersion(unifiedCurrentReleaseVersion);

    String unifiedNewReleaseVersion = unifyReleaseVersion(releaseLetterModelRequest.getReleaseVersion());
    if (!unifiedCurrentReleaseVersion.equals(unifiedNewReleaseVersion) && isReleaseVersionExisted(
            unifiedNewReleaseVersion)) {
      throw new AlreadyExistedException(ErrorCode.RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.getCode(),
          ErrorCode.RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.getHelpText());
    }

    foundReleaseLetter.setContent(releaseLetterModelRequest.getContent());
    foundReleaseLetter.setReleaseVersion(unifiedNewReleaseVersion);

    return releaseLetterRepository.save(foundReleaseLetter);
  }

  private boolean isReleaseVersionExisted(String requestedReleaseVersion) {
    return releaseLetterRepository.existsByReleaseVersion(unifyReleaseVersion(requestedReleaseVersion));
  }

  private String unifyReleaseVersion(String originalInputVersion) {
    return originalInputVersion.trim().toUpperCase();
  }
}
