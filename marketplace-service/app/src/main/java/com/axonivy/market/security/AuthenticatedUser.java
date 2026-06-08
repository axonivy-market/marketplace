package com.axonivy.market.security;

public record AuthenticatedUser(
    String gitHubUserId,
    String username,
    String name,
    String avatarUrl,
    String provider,
    boolean admin
) {
}
