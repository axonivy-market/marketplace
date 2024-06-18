//package com.axonivy.market.controller;
//
//import com.axonivy.market.entity.User;
//import com.axonivy.market.repository.UserRepository;
//import com.axonivy.market.service.UserService;
//import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
//import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.servlet.view.RedirectView;
//
//@RestController
//public class OAuth2Controller {
//  private final OAuth2AuthorizedClientService clientService;
//  private final UserService userService;
//
//  public OAuth2Controller(OAuth2AuthorizedClientService clientService, UserService userService) {
//    this.clientService = clientService;
//    this.userService = userService;
//  }
//
//  @GetMapping("/login/oauth2/code/{provider}")
//  public RedirectView loginSuccess(@PathVariable String provider, OAuth2AuthenticationToken authenticationToken) {
//    OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
//        authenticationToken.getAuthorizedClientRegistrationId(),
//        authenticationToken.getName()
//    );
//
//
//
//
//
//    return new RedirectView("/login-success");
//  }
//}