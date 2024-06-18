package com.axonivy.market.service;

import com.axonivy.market.entity.User;

import java.util.Map;

public interface GitHubService {
    Map<String, Object> getAccessToken(String code, String clientId, String clientSecret);

    User getAndUpdateUser(String accessToken);
}
