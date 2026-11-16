package com.vencentdev.backend.modules.auth.service;

import com.vencentdev.backend.common.exception.ConflictException;
import com.vencentdev.backend.config.AuthSessionProperties;
import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import com.vencentdev.backend.modules.auth.dto.AuthTokenResponse;
import com.vencentdev.backend.modules.auth.dto.ManualLoginRequest;
import com.vencentdev.backend.modules.auth.dto.ManualSignupRequest;
import com.vencentdev.backend.modules.auth.session.AuthSession;
import com.vencentdev.backend.modules.auth.session.AuthSessionRepository;
import com.vencentdev.backend.modules.user.entity.User;
import com.vencentdev.backend.modules.user.enums.ContactVerificationStatus;
import com.vencentdev.backend.modules.user.enums.IdentityLevel;
import com.vencentdev.backend.modules.user.enums.Role;
import com.vencentdev.backend.modules.user.enums.UserType;
import com.vencentdev.backend.modules.user.mapper.UserMapper;
import com.vencentdev.backend.modules.user.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ManualAuthService {

  private static final String LOCAL_EXTERNAL_ID_PREFIX = "local:";
  private static final String MANUAL_TOKEN_PREFIX = "vs_";

  private final UserRepository users;
  private final AuthSessionRepository sessions;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final AuthSessionProperties sessionProperties;
  private final SecureRandom secureRandom = new SecureRandom();
  private final Clock clock;

  @Autowired
  public ManualAuthService(
      UserRepository users,
      AuthSessionRepository sessions,
      UserMapper userMapper,
      PasswordEncoder passwordEncoder,
      AuthSessionProperties sessionProperties) {
    this(users, sessions, userMapper, passwordEncoder, sessionProperties, Clock.systemUTC());
  }

  ManualAuthService(
      UserRepository users,
      AuthSessionRepository sessions,
      UserMapper userMapper,
      PasswordEncoder passwordEncoder,
      AuthSessionProperties sessionProperties,
      Clock clock) {
    this.users = users;
    this.sessions = sessions;
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
    this.sessionProperties = sessionProperties;
    this.clock = clock;
  }

  @Transactional
  public AuthTokenResponse signup(ManualSignupRequest request) {
    String email = normalizeEmail(request.email());
    if (users.findByEmailIgnoreCase(email).isPresent()) {
      throw new ConflictException("An account with this email already exists");
    }

    User user =
        User.builder()
            .externalId(LOCAL_EXTERNAL_ID_PREFIX + UUID.randomUUID())
            .email(email)
            .displayName(request.displayName().trim())
            .passwordHash(encodePassword(request.password()))
            .role(Role.USER)
            .userType(UserType.INDIVIDUAL)
            .identityLevel(IdentityLevel.I0)
            .contactVerificationStatus(ContactVerificationStatus.UNVERIFIED)
            .build();

    return issueToken(users.save(user));
  }

  @Transactional
  public AuthTokenResponse login(ManualLoginRequest request) {
    String email = normalizeEmail(request.email());
    User user =
        users
            .findByEmailIgnoreCase(email)
            .filter(candidate -> candidate.getPasswordHash() != null)
            .filter(
                candidate ->
                    passwordEncoder.matches(
                        passwordDigest(request.password()), candidate.getPasswordHash()))
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

    return issueToken(user);
  }

  @Transactional(readOnly = true)
  public AuthenticatedUser resolveSession(String accessToken) {
    String tokenHash = sha256(accessToken);
    Instant now = clock.instant();
    return sessions
        .findByTokenHashAndRevokedAtIsNull(tokenHash)
        .filter(session -> session.getExpiresAt().isAfter(now))
        .flatMap(session -> users.findById(session.getUserId()))
        .map(
            user ->
                new AuthenticatedUser(
                    user.getExternalId(),
                    user.getEmail(),
                    user.getDisplayName(),
                    Set.of(user.getRole().name())))
        .orElse(null);
  }

  public boolean isManualSessionToken(String accessToken) {
    return accessToken.startsWith(MANUAL_TOKEN_PREFIX);
  }

  @Transactional
  public void revokeSession(String accessToken) {
    sessions
        .findByTokenHash(sha256(accessToken))
        .ifPresent(session -> session.revoke(clock.instant()));
  }

  private AuthTokenResponse issueToken(User user) {
    String accessToken = generateToken();
    Instant expiresAt = clock.instant().plus(sessionProperties.getSessionTtl());
    sessions.save(
        AuthSession.builder()
            .userId(user.getId())
            .tokenHash(sha256(accessToken))
            .expiresAt(expiresAt)
            .build());
    return new AuthTokenResponse(accessToken, "Bearer", expiresAt, userMapper.toResponse(user));
  }

  private String generateToken() {
    byte[] tokenBytes = new byte[32];
    secureRandom.nextBytes(tokenBytes);
    return MANUAL_TOKEN_PREFIX
        + Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
  }

  private String encodePassword(String password) {
    return passwordEncoder.encode(passwordDigest(password));
  }

  private String passwordDigest(String password) {
    return sha256(password);
  }

  private String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private String sha256(String value) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256")
              .digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder(digest.length * 2);
      for (byte item : digest) {
        hex.append(String.format("%02x", item));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }
}
