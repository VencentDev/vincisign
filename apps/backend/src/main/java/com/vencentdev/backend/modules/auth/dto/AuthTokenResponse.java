package com.vencentdev.backend.modules.auth.dto;

import com.vencentdev.backend.modules.user.dto.UserResponse;
import java.time.Instant;

public record AuthTokenResponse(
    String accessToken, String tokenType, Instant expiresAt, UserResponse user) {}
