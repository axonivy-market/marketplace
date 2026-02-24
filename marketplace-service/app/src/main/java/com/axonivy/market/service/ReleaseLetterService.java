package com.axonivy.market.service;

import com.axonivy.market.entity.ReleaseLetter;
import com.axonivy.market.model.ReleaseLetterModelRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReleaseLetterService {
  Page<ReleaseLetter> findAllReleaseLetters(Pageable pageable);

  List<ReleaseLetter> findAllReleaseLettersWithoutPaging();

  ReleaseLetter findReleaseLetterById(String id);

  Page<ReleaseLetter> findLatestReleaseLetter(Pageable pageable);

  ReleaseLetter findReleaseLetterBySprint(String releaseVersion);

  ReleaseLetter createReleaseLetter(ReleaseLetterModelRequest releaseLetterModelRequest);

  ReleaseLetter updateReleaseLetter(String releaseVersion, ReleaseLetterModelRequest releaseLetterModelRequest);

  void deleteReleaseLetterBySprint(String sprint);
}
