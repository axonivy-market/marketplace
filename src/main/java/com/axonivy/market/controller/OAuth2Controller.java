package com.axonivy.market.controller;

import com.axonivy.market.entity.User;
import com.axonivy.market.model.Oauth2AuthorizationCode;
import com.axonivy.market.service.GitHubService;
import com.axonivy.market.service.JwtService;
import com.axonivy.market.service.impl.JwtServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class OAuth2Controller {

  @Value("${spring.security.oauth2.client.registration.github.client-id}")
  private String clientId;

  @Value("${spring.security.oauth2.client.registration.github.client-secret}")
  private String clientSecret;

  private final GitHubService gitHubService;

  private final JwtService jwtService;

  public OAuth2Controller(GitHubService gitHubService, JwtService jwtService) {
    this.gitHubService = gitHubService;
    this.jwtService = jwtService;
  }

  @PostMapping("/exchange-code")
  public ResponseEntity<?> exchangeCodeForToken(@RequestBody Oauth2AuthorizationCode oauth2AuthorizationCode, HttpServletResponse response) {
    Map<String, Object> tokenResponse = gitHubService.getAccessToken(oauth2AuthorizationCode.getCode(), clientId, clientSecret);
    String accessToken = (String) tokenResponse.get("access_token");

    User user = gitHubService.getAndUpdateUser(accessToken);

    // Generate JWT
    String jwtToken = jwtService.generateToken(user);

//    // Create HTTP-only cookie with JWT
//    ResponseCookie cookie = ResponseCookie.from("token", jwtToken)
//        .httpOnly(false)
//        .path("/")
//        .maxAge(365L * 86400)
//        .build();
//
//    // Add cookie to response
//    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    // Return the JWT in the response body if needed
    return ResponseEntity.ok().body(Collections.singletonMap("token", jwtToken));
  }
}