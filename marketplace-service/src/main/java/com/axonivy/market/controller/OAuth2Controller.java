package com.axonivy.market.controller;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.github.model.GitHubAccessTokenResponse;
import com.axonivy.market.github.model.GitHubProperty;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.Oauth2AuthorizationCode;
import com.axonivy.market.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.axonivy.market.constants.RequestMappingConstants.AUTH;
import static com.axonivy.market.constants.RequestMappingConstants.GIT_HUB_LOGIN;

@RestController
@RequestMapping(AUTH)
public class OAuth2Controller {

  private final GitHubProperty gitHubProperty;

  private final GitHubService gitHubService;

  private final JwtService jwtService;

  public OAuth2Controller(GitHubService gitHubService, JwtService jwtService, GitHubProperty gitHubProperty) {
    this.gitHubService = gitHubService;
    this.jwtService = jwtService;
    this.gitHubProperty = gitHubProperty;
  }

  @PostMapping(GIT_HUB_LOGIN)
  @Operation(description = "Get rating authentication token")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully login to GitHub provider",
          content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
      @ApiResponse(responseCode = "400", description = "Bad Request")})
  @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
      schema = @Schema(implementation = Oauth2AuthorizationCode.class)))
  public ResponseEntity<Map<String, String>> gitHubLogin(@RequestBody Oauth2AuthorizationCode oauth2AuthorizationCode) {
    String accessToken;
    try {
      GitHubAccessTokenResponse tokenResponse = gitHubService.getAccessToken(oauth2AuthorizationCode.getCode(),
          gitHubProperty);
      accessToken = tokenResponse.getAccessToken();
      GithubUser githubUser = gitHubService.getAndUpdateUser(accessToken);
      String jwtToken = jwtService.generateToken(githubUser, accessToken);
      return new ResponseEntity<>(Collections.singletonMap(GitHubConstants.Json.TOKEN, jwtToken), HttpStatus.OK);
    } catch (Oauth2ExchangeCodeException e) {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put(CommonConstants.ERROR, e.getError());
      errorResponse.put(CommonConstants.MESSAGE, e.getErrorDescription());
      return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      return new ResponseEntity<>(Map.of(CommonConstants.MESSAGE, e.getMessage()), HttpStatus.BAD_REQUEST);
    }
  }
}