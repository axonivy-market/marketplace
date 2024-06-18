//package com.axonivy.market.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.oauth2.client.registration.ClientRegistration;
//import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
//import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
//import org.springframework.security.oauth2.core.AuthorizationGrantType;
//import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
//
//@Configuration
//public class OAuth2Config {
//  @Value("${spring.security.oauth2.client.registration.github.client-id}")
//  private String githubClientId;
//
//  @Value("${spring.security.oauth2.client.registration.github.client-secret}")
//  private String githubClientSecret;
//
//  @Value("${spring.security.oauth2.client.registration.github.redirect-uri}")
//  private String githubRedirectUri;
//
//  @Bean
//  public ClientRegistrationRepository clientRegistrationRepository() {
//    return new InMemoryClientRegistrationRepository(getGithubClientRegistration());
//  }
//
//  public ClientRegistration getGithubClientRegistration() {
//    return ClientRegistration.withRegistrationId("github")
//        .clientId(githubClientId)
//        .clientSecret(githubClientSecret)
//        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
//        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
//        .redirectUri(githubRedirectUri)
//        .scope("read:user", "user:email")
//        .authorizationUri("https://github.com/login/oauth/authorize")
//        .tokenUri("https://github.com/login/oauth/access_token")
//        .userInfoUri("https://api.github.com/user")
//        .userNameAttributeName("id")
//        .clientName("GitHub")
//        .build();
//  }
//}