package com.axonivy.market.github.service;

import com.axonivy.market.core.entity.Product;
import com.axonivy.market.criteria.ProductSecurityCriteria;
import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.enums.PullRequestAction;
import com.axonivy.market.exceptions.model.MissingHeaderException;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.exceptions.model.UnauthorizedException;
import com.axonivy.market.github.model.GitHubAccessTokenResponse;
import com.axonivy.market.github.model.GitHubProperty;
import com.axonivy.market.entity.ProductSecurityInfo;
import com.axonivy.market.model.AlternativeExtensionData;
import com.axonivy.market.model.GitHubReleaseModel;
import com.axonivy.market.model.UserInfo;
import org.kohsuke.github.GHArtifact;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.kohsuke.github.GHWorkflowRun;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GHPullRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface GitHubService {

  /**
   * <p>
   * Initializes and returns a GitHub API client using the configured service account token.
   * The returned GitHub object can be used for authenticated API calls to GitHub.
   * </p>
   *
   * @return {@link GitHub} - authenticated GitHub API client instance
   * @throws IOException - if GitHub API connection fails
   * @author ntqdinh
   */
  GitHub getGitHub() throws IOException;

  /**
   * <p>
   * Initializes and returns a GitHub API client using the provided user access token.
   * Allows making API calls on behalf of the specified user with their permissions.
   * </p>
   *
   * @param  accessToken
   *              type {@link String} - the GitHub OAuth2 user access token for authentication
   * @return {@link GitHub} - authenticated GitHub API client instance with user permissions
   * @throws IOException - if GitHub API connection fails
   * @author ntqdinh
   */
  GitHub getGitHub(String accessToken) throws IOException;

  /**
   * <p>
   * Retrieves a GitHub organization by name. Returns organization information including
   * members, teams, repositories, and organization-level settings.
   * </p>
   *
   * @param  orgName
   *              type {@link String} - the GitHub organization name (e.g., "axonivy")
   * @return {@link GHOrganization} - organization object with members, teams, and settings
   * @throws IOException - if GitHub API call fails or organization not found
   * @author ntqdinh
   */
  GHOrganization getOrganization(String orgName) throws IOException;

  /**
   * <p>
   * Retrieves a GitHub repository by path. Returns repository information including
   * branches, releases, workflows, and repository settings.
   * </p>
   *
   * @param  repositoryPath
   *              type {@link String} - the repository path (format: "owner/repo")
   * @return {@link GHRepository} - repository object with metadata and configuration
   * @throws IOException - if GitHub API call fails or repository not found
   * @author ntqdinh
   */
  GHRepository getRepository(String repositoryPath) throws IOException;

  /**
   * <p>
   * Retrieves all release tags from a GitHub repository. Returns version tags used
   * to identify product releases and versions.
   * </p>
   *
   * @param  repositoryPath
   *              type {@link String} - the repository path (format: "owner/repo")
   * @return {@link List<GHTag>} - list of all release tags in the repository
   * @throws IOException - if GitHub API call fails
   * @author ntqdinh
   */
  List<GHTag> getRepositoryTags(String repositoryPath) throws IOException;

  /**
   * <p>
   * Retrieves the contents of a directory in a GitHub repository. Returns list of files
   * and subdirectories with their metadata, optionally from a specific branch/ref.
   * </p>
   *
   * @param  ghRepository
   *              type {@link GHRepository} - the GitHub repository object
   * @param  path
   *              type {@link String} - the directory path within the repository
   * @param  ref
   *              type {@link String} - the branch, tag, or commit SHA to read from (null for default branch)
   * @return {@link List<GHContent>} - list of files and directories in the specified path
   * @throws IOException - if GitHub API call fails or path not found
   * @author ntqdinh
   */
  List<GHContent> getDirectoryContent(GHRepository ghRepository, String path, String ref) throws IOException;

  /**
   * <p>
   * Retrieves a single file or directory from a GitHub repository. Returns file content,
   * size, encoding, and metadata for a specific path.
   * </p>
   *
   * @param  ghRepository
   *              type {@link GHRepository} - the GitHub repository object
   * @param  path
   *              type {@link String} - the file path within the repository
   * @param  ref
   *              type {@link String} - the branch, tag, or commit SHA to read from (null for default branch)
   * @return {@link GHContent} - the file/directory content with metadata
   * @throws IOException - if GitHub API call fails or file not found
   * @author ntqdinh
   */
  GHContent getGHContent(GHRepository ghRepository, String path, String ref) throws IOException;

  /**
   * <p>
   * Exchanges an OAuth2 authorization code for a GitHub access token. Completes the
   * OAuth2 login flow by exchanging the temporary auth code for a persistent user token.
   * </p>
   *
   * @param  code
   *              type {@link String} - the OAuth2 authorization code from GitHub
   * @param  gitHubProperty
   *              type {@link GitHubProperty} - GitHub OAuth2 app configuration (client ID, secret)
   * @return {@link GitHubAccessTokenResponse} - response containing the access token and user info
   * @throws Oauth2ExchangeCodeException - if token exchange fails or code is invalid
   * @throws MissingHeaderException - if required headers are missing from GitHub response
   * @author ntqdinh
   */
  GitHubAccessTokenResponse getAccessToken(String code, GitHubProperty gitHubProperty)
      throws Oauth2ExchangeCodeException, MissingHeaderException;

  /**
   * <p>
   * Retrieves GitHub user information and creates or updates the user record in the system.
   * Stores user profile data including GitHub ID, username, and profile information.
   * </p>
   *
   * @param  accessToken
   *              type {@link String} - the GitHub user access token for authentication
   * @return {@link GithubUser} - created or updated GitHub user record in the system
   * @author ntqdinh
   */
  GithubUser getAndUpdateUser(String accessToken);

  /**
   * <p>
   * Validates that a user belongs to a specific GitHub organization and team.
   * Checks membership and team assignment for authorization purposes.
   * </p>
   *
   * @param  accessToken
   *              type {@link String} - the GitHub user access token
   * @param  org
   *              type {@link String} - the GitHub organization name to check membership in
   * @param  team
   *              type {@link String} - the GitHub team name within the organization
   * @return {@link UserInfo} - user information confirming membership in the org/team
   * @throws UnauthorizedException - if user is not a member of the specified org/team
   * @author ntqdinh
   */
  UserInfo validateUserInOrganizationAndTeam(String accessToken, String org, String team) throws UnauthorizedException;

  /**
   * <p>
   * Searches for product security details based on the provided criteria and returns a paginated result.
   * Useful for filtering and retrieving security information for products according to custom search parameters.
   * </p>
   *
   * @param criteria type {@link ProductSecurityCriteria} - the search criteria for filtering product security details
   * @param pageable type {@link Pageable} - pagination and sorting information
   * @return {@link Page}<{@link ProductSecurityInfo}> - paginated list of product security details matching the 
   * criteria
   * @throws IOException - if an error occurs while accessing security details
   * @author tvtphuc
   */
  Page<ProductSecurityInfo> searchSecurityDetails(ProductSecurityCriteria criteria, Pageable pageable)
      throws IOException;

  /**
   * <p>
   * Synchronizes and retrieves the latest security details for all products from GitHub repositories.
   * This method fetches and updates security information for each product, ensuring up-to-date data.
   * </p>
   *
   * @return {@link List}<{@link ProductSecurityInfo}> - list of updated product security details
   * @throws IOException - if an error occurs during synchronization or data retrieval
   * @author tvtphuc
   */
  List<ProductSecurityInfo> syncSecurityDetailsForProduct() throws IOException;

  /**
   * <p>
   * Converts a list of GitHub release objects into paginated release models with
   * formatted information for marketplace display.
   * </p>
   *
   * @param  ghReleases
   *              type {@link List<GHRelease>} - list of GitHub release objects from API
   * @param  pageable
   *              type {@link Pageable} - pagination configuration
   * @param  productId
   *              type {@link String} - the product ID to associate releases with
   * @param  productRepoName
   *              type {@link String} - the product repository name for context
   * @param  productSourceUrl
   *              type {@link String} - the source URL for the product repository
   * @return {@link Page<GitHubReleaseModel>} - paginated list of formatted release models
   * @throws IOException - if processing releases fails
   * @author ntqdinh
   */
  Page<GitHubReleaseModel> getGitHubReleaseModels(List<GHRelease> ghReleases,
      Pageable pageable, String productId, String productRepoName, String productSourceUrl) throws IOException;

  /**
   * <p>
   * Retrieves official releases from a GitHub repository. Returns only non-draft,
   * published releases suitable for product distribution.
   * </p>
   *
   * @param  repoName
   *              type {@link String} - the repository name (format: "owner/repo")
   * @param  productId
   *              type {@link String} - the product ID for context and logging
   * @return {@link List<GHRelease>} - list of official releases from the repository
   * @throws IOException - if GitHub API call fails
   * @author ntqdinh
   */
  List<GHRelease> getRepoOfficialReleases(String repoName, String productId) throws IOException;

  /**
   * <p>
   * Retrieves a specific GitHub release for a product by release ID. Returns complete
   * release information including assets, notes, and metadata.
   * </p>
   *
   * @param  product
   *              type {@link Product} - the product entity for context
   * @param  releaseId
   *              type {@link long} - the GitHub release ID to retrieve
   * @return {@link GitHubReleaseModel} - formatted release model with all details
   * @throws IOException - if GitHub API call fails or release not found
   * @author ntqdinh
   */
  GitHubReleaseModel getGitHubReleaseModelByProductIdAndReleaseId(Product product, long releaseId) throws IOException;

  /**
   * <p>
   * Retrieves the latest successful workflow run for a specific workflow file in a repository.
   * Used to get the most recent CI/CD execution results.
   * </p>
   *
   * @param  repo
   *              type {@link GHRepository} - the GitHub repository to search
   * @param  workflowFileName
   *              type {@link String} - the workflow file name (e.g., "build.yml")
   * @return {@link GHWorkflowRun} - the latest successful workflow run
   * @throws IOException - if GitHub API call fails or workflow not found
   * @author ntqdinh
   */
  GHWorkflowRun getLatestWorkflowRun(GHRepository repo, String workflowFileName) throws IOException;

  /**
   * <p>
   * Retrieves the test report artifact from a workflow run. Extracts the artifact
   * containing test results and logs from CI/CD execution.
   * </p>
   *
   * @param  run
   *              type {@link GHWorkflowRun} - the workflow run to get artifacts from
   * @return {@link GHArtifact} - the test report artifact from the workflow run
   * @throws IOException - if GitHub API call fails or artifact not found
   * @author ntqdinh
   */
  GHArtifact getExportTestArtifact(GHWorkflowRun run) throws IOException;

  /**
   * <p>
   * Downloads a workflow artifact as a ZIP file stream. Returns the artifact content
   * ready for extraction and analysis.
   * </p>
   *
   * @param  artifact
   *              type {@link GHArtifact} - the GitHub artifact to download
   * @return {@link InputStream} - input stream of the artifact ZIP file
   * @throws IOException - if artifact download fails
   * @author ntqdinh
   */
  InputStream downloadArtifactZip(GHArtifact artifact) throws IOException;

  /**
   * <p>
   * Create PullRequest to update the README file in the specified repository to include successor notes, according to
   * the given pull request action.
   * This method creates or updates a pull request that documents successor information in the repository's README.
   * </p>
   *
   * @param repositoryPath type {@link String} - the path or name of the GitHub repository
   * @param action         type {@link PullRequestAction} - the action to perform on the pull request (e.g., open,
   *                       update,close)
   * @return {@link GHPullRequest} - the created or updated pull request for the README changes
   * @throws IOException - if an error occurs while updating the README or interacting with GitHub
   * @author tvtphuc
   */
  GHPullRequest updateReadmeForSuccessorNotes(String repoPath, PullRequestAction action,
      AlternativeExtensionData marketplaceData) throws IOException;

  void archiveTheRepository(String repoPath) throws IOException;

  void unArchivedTheRepository();

  boolean hasDeprecationWarningInReadme(String repoPath) throws IOException;
}
