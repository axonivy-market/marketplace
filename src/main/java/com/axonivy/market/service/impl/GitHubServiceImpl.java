package com.axonivy.market.service.impl;

import com.axonivy.market.entity.User;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.repository.UserRepository;
import com.axonivy.market.service.GitHubService;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Service
public class GitHubServiceImpl implements GitHubService {
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    public GitHubServiceImpl(RestTemplateBuilder restTemplateBuilder, UserRepository userRepository) {
        this.restTemplate = restTemplateBuilder.build();
        this.userRepository = userRepository;
    }

    public Map<String, Object> getAccessToken(String code, String clientId, String clientSecret) throws Oauth2ExchangeCodeException {
        String url = "https://github.com/login/oauth/access_token";
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
        if (response.getBody().containsKey("error")) {
            throw new Oauth2ExchangeCodeException(response.getBody().get("error").toString(), response.getBody().get("error_description").toString());
        }
        return response.getBody();
    }

    public User getAndUpdateUser(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.github.com/user", HttpMethod.GET, entity, Map.class);

        Map<String, Object> userDetails = response.getBody();

        if (userDetails == null) {
            throw new RuntimeException("Failed to fetch user details from GitHub");
        }

        String gitHubId = userDetails.get("id").toString();
        String name = (String) userDetails.get("name");
        String avatarUrl = (String) userDetails.get("avatar_url");
        String username = (String) userDetails.get("login");

        User user = userRepository.searchByGitHubId(gitHubId);
        if (user == null) {
            user = new User();
        }
        user.setGitHubId(gitHubId);
        user.setName(name);
        user.setUsername(username);
        user.setAvatarUrl(avatarUrl);
        user.setProvider("GitHub");

        userRepository.save(user);

        return user;
    }
}
