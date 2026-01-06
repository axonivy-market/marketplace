package com.axonivy.market.controller;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.model.Oauth2AuthorizationCode;
import com.axonivy.market.service.OAuth2Service;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.axonivy.market.constants.RequestMappingConstants.AUTH;
import static com.axonivy.market.constants.RequestMappingConstants.GITHUB_LOGIN;
import static com.axonivy.market.constants.RequestMappingConstants.GITHUB_REQUEST_ACCESS;
import static org.springframework.http.HttpHeaders.*;

import org.springframework.web.bind.annotation.PutMapping;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping(AUTH)
public class OAuth2Controller {

  private final OAuth2Service oAuth2Service;

  @PostMapping(GITHUB_LOGIN)
  @Operation(description = "Get rating authentication token")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully login to GitHub provider",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
      @ApiResponse(responseCode = "400", description = "Bad Request") })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
      schema = @Schema(implementation = Oauth2AuthorizationCode.class)))
  @Hidden
  public ResponseEntity<Map<String, String>> gitHubLogin(@RequestBody Oauth2AuthorizationCode oauth2AuthorizationCode) {
    String jwt = oAuth2Service.loginToGitHubAndGetJWT(oauth2AuthorizationCode);
    return new ResponseEntity<>(Map.of(GitHubConstants.Json.TOKEN, jwt), HttpStatus.OK);
  }

  @PostMapping(GITHUB_REQUEST_ACCESS)
  @Hidden
  public ResponseEntity<Map<String, String>> requestAccess(@RequestHeader(value = AUTHORIZATION) String authorizationHeader) {
    String jwt = oAuth2Service.validateTokenAndGenerateJWT(authorizationHeader);
    return new ResponseEntity<>(Map.of(GitHubConstants.Json.TOKEN, jwt), HttpStatus.OK);
  }
}
