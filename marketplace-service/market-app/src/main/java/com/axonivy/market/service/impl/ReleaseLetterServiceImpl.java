package com.axonivy.market.service.impl;

import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.enums.ErrorCode;
import com.axonivy.market.core.exceptions.model.NotFoundException;
import com.axonivy.market.entity.ReleaseLetter;
import com.axonivy.market.exceptions.model.AlreadyExistedException;
import com.axonivy.market.model.ReleaseLetterModelRequest;
import com.axonivy.market.repository.ReleaseLetterRepository;
import com.axonivy.market.service.ReleaseLetterService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;

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
  public Page<ReleaseLetter> findActiveReleaseLetter(Pageable pageable) {
    return releaseLetterRepository.findByIsActive(true, pageable);
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
    System.out.println("Is letter model active: " + releaseLetterModelRequest.isActive());
    String unifiedSprint = unifySprint(releaseLetterModelRequest.getSprint());
    if (isSprintExisted(unifiedSprint)) {
      throw new AlreadyExistedException(ErrorCode.RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.getCode(),
          ErrorCode.RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.getHelpText());
    }

    ReleaseLetter releaseLetter =
        ReleaseLetter.builder().content(releaseLetterModelRequest.getContent()).sprint(
            unifiedSprint).isActive(releaseLetterModelRequest.isActive()).build();

    if (releaseLetterModelRequest.isActive()) {
//      this.handleActiveReleaseLetter();
      releaseLetterRepository.deactivateOtherActiveReleaseLetters(unifiedSprint);
    }

    return releaseLetterRepository.save(releaseLetter);
  }

  @Override
  public ReleaseLetter updateReleaseLetter(String selectedSprint, ReleaseLetterModelRequest releaseLetterModelRequest) {
    String unifiedSelectedSprint = unifySprint(selectedSprint);
    var foundReleaseLetter = findReleaseLetterBySprint(unifiedSelectedSprint);

    String unifiedNewSprint = unifySprint(releaseLetterModelRequest.getSprint());
    if (!unifiedSelectedSprint.equals(unifiedNewSprint) && isSprintExisted(
        unifiedNewSprint)) {
      throw new AlreadyExistedException(ErrorCode.RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.getCode(),
          ErrorCode.RELEASE_LETTER_RELEASE_VERSION_ALREADY_EXISTED.getHelpText());
    }

    foundReleaseLetter.setActive(releaseLetterModelRequest.isActive());
    foundReleaseLetter.setContent(releaseLetterModelRequest.getContent());
    foundReleaseLetter.setSprint(unifiedNewSprint);

    if (releaseLetterModelRequest.isActive()) {
      releaseLetterRepository.deactivateOtherActiveReleaseLetters(unifiedNewSprint);
    }

    return releaseLetterRepository.save(foundReleaseLetter);
  }

  private boolean isSprintExisted(String requestedSprint) {
    return releaseLetterRepository.existsBySprint(unifySprint(requestedSprint));
  }

  private String unifySprint(String originalInputSprint) {
    return originalInputSprint.trim().toUpperCase();
  }
}
