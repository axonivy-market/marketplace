package com.axonivy.market.service;

import com.axonivy.market.entity.ReleaseLetter;
import com.axonivy.market.entity.ReleaseLetterDraft;
import com.axonivy.market.model.ReleaseLetterDraftModel;
import com.axonivy.market.model.ReleaseLetterModelRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReleaseLetterService {

  /**
   * <p>
   * Retrieves a paginated list of all release letters from the database, optionally returning all if not read-only,
   * ordered by creation date in descending order.
   * </p>
   *
   * @param pageable   type {@link Pageable} - pagination and sorting information
   * @param isReadOnly type {@link boolean} - flag to determine if all records should be returned without pagination
   * @return {@link Page<ReleaseLetter>} - a page of release letters
   * @author vhhoang
   */
  Page<ReleaseLetter> findAllReleaseLetters(Pageable pageable, boolean isReadOnly);

  /**
   * <p>
   * Retrieves a single release letter by its unique identifier from the database.
   * </p>
   *
   * @param id type {@link String} - the unique identifier of the release letter
   * @return {@link ReleaseLetter} - the release letter
   * @author vhhoang
   */
  ReleaseLetter findReleaseLetterById(String id);

  /**
   * <p>
   * Retrieves a paginated list of the latest release letters marked as latest from the database.
   * </p>
   *
   * @param pageable type {@link Pageable} - pagination and sorting information
   * @return {@link Page<ReleaseLetter>} - a page of the latest release letters
   * @author vhhoang
   */
  Page<ReleaseLetter> findLatestReleaseLetter(Pageable pageable);

  /**
   * <p>
   * Creates a new release letter based on the provided request data, validates the sprint, checks for duplicates,
   * and handles latest flag deactivation for the same sprint.
   * </p>
   *
   * @param releaseLetterModelRequest type {@link ReleaseLetterModelRequest} - the request data containing sprint,
   *                                  content, and latest flag
   * @param isDraft                   indicates whether the release letter should be saved
   *                                  as a draft
   * @return {@link ReleaseLetter} - the created release letter
   * @author vhhoang
   */
  ReleaseLetter createReleaseLetter(ReleaseLetterModelRequest releaseLetterModelRequest, boolean isDraft);

  /**
   * <p>
   * Updates an existing release letter by its ID with new data from the request, validates the sprint, checks for
   * duplicates, and handles latest flag deactivation.
   * </p>
   *
   * @param id                        type {@link String} - the unique identifier of the release letter to update
   * @param releaseLetterModelRequest type {@link ReleaseLetterModelRequest} - the request data containing updated
   *                                  sprint, content, and latest flag
   * @param gitHubUserId              type {@link String} - the GitHub user id taken from the request
   * @return {@link ReleaseLetter} - the updated release letter
   * @author vhhoang
   */
  ReleaseLetter updateReleaseLetter(String id, ReleaseLetterModelRequest releaseLetterModelRequest,
      String gitHubUserId);

  /**
   * <p>
   * Deletes a release letter by its unique ID. Removes the release letter completely from the database and
   * associated systems. Operation is permanent and cannot be undone.
   * </p>
   *
   * @param id type {@link String} - the unique ID of the release letter to delete
   * @author vhhoang
   */
  void deleteReleaseLetterById(String id);

  /**
   * <p>
   * Saves the provided release letter content as a draft for the given GitHub user. This operation writes draft
   * data to the persistence layer and may create a new draft entry or update an existing one for the same user.
   * </p>
   *
   * @param releaseLetterModelRequest type {@link ReleaseLetterModelRequest} - the request data to be stored as draft
   *                                  content
   * @param gitHubUserId              type {@link String} - the GitHub user id that owns the draft
   * @return {@link ReleaseLetterDraftModel} - the persisted draft after the save operation
   * @author vhhoang
   */
  ReleaseLetterDraftModel saveAsDraft(ReleaseLetterModelRequest releaseLetterModelRequest, String gitHubUserId);

  /**
   * <p>
   * Retrieves draft content for the given GitHub user and release letter identifier. If no matching draft exists,
   * this method returns {@code null}.
   * </p>
   *
   * @param gitHubUserId    type {@link String} - the GitHub user id that owns the draft
   * @param releaseLetterId type {@link String} - the release letter identifier associated with the draft
   * @return {@link ReleaseLetterDraft} - the matching draft, or {@code null} if no draft exists
   * @author vhhoang
   */
  ReleaseLetterDraft getDraftContentByGitHubUserIdAndReleaseLetterId(String gitHubUserId, String releaseLetterId);

  /**
   * <p>
   * Deletes the draft associated with the given GitHub user and release letter identifier. This operation removes
   * the matching draft from the persistence layer. If no matching draft exists, the operation has no effect.
   * </p>
   *
   * @param gitHubUserId    type {@link String} - the GitHub user id that owns the draft
   * @param releaseLetterId type {@link String} - the release letter identifier associated with the draft
   * @author vhhoang
   */
  void deleteDraftByGitHubUserIdAndReleaseLetterId(String gitHubUserId, String releaseLetterId);
}
