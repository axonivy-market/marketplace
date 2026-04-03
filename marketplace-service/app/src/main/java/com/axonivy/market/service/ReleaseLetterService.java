package com.axonivy.market.service;

import com.axonivy.market.entity.ReleaseLetter;
import com.axonivy.market.model.ReleaseLetterModelRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReleaseLetterService {

  /**
   * <p>
   * Find all release letters
   * </p>
   *
   * @param  pageable
   *              type {@link Pageable}
   * @param  isReadOnly
   *              type {@link boolean}
   * @return {@link Page<ReleaseLetter>}
   * @author vhhoang
   */
  Page<ReleaseLetter> findAllReleaseLetters(Pageable pageable, boolean isReadOnly);

  /**
   * <p>
   * Find release letter by id
   * </p>
   *
   * @param  id
   *              type {@link String}
   * @return {@link ReleaseLetter}
   * @author vhhoang
   */
  ReleaseLetter findReleaseLetterById(String id);

  /**
   * <p>
   * Find latest release letter
   * </p>
   *
   * @param  pageable
   *              type {@link Pageable}
   * @return {@link Page<ReleaseLetter>}
   * @author vhhoang
   */
  Page<ReleaseLetter> findLatestReleaseLetter(Pageable pageable);

  /**
   * <p>
   * Create release letter
   * </p>
   *
   * @param  releaseLetterModelRequest
   *              type {@link ReleaseLetterModelRequest}
   * @return {@link ReleaseLetter}
   * @author vhhoang
   */
  ReleaseLetter createReleaseLetter(ReleaseLetterModelRequest releaseLetterModelRequest);

  /**
   * <p>
   * Update release letter
   * </p>
   *
   * @param  id
   *              type {@link String}
   * @param  releaseLetterModelRequest
   *              type {@link ReleaseLetterModelRequest}
   * @return {@link ReleaseLetter}
   * @author vhhoang
   */
  ReleaseLetter updateReleaseLetter(String id, ReleaseLetterModelRequest releaseLetterModelRequest);

  /**
   * <p>
   * Delete release letter by id
   * </p>
   *
   * @param  id
   *              type {@link String}
   * @return {@link }
   * @author vhhoang
   */
  void deleteReleaseLetterById(String id);
}
