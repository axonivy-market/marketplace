package com.axonivy.market.model;

import com.axonivy.market.entity.GithubUser;

public record AdminLoginResponse(String token, GithubUser user) {
}
