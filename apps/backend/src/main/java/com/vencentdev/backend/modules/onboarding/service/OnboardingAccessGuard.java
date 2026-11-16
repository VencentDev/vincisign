package com.vencentdev.backend.modules.onboarding.service;

import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component("onboardingAccessGuard")
public class OnboardingAccessGuard {

  private final OnboardingService onboarding;

  public OnboardingAccessGuard(OnboardingService onboarding) {
    this.onboarding = onboarding;
  }

  public boolean isComplete(Authentication authentication) {
    AuthenticatedUser user = authenticatedUser(authentication);
    return user != null && onboarding.status(user).completed();
  }

  private AuthenticatedUser authenticatedUser(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }

    if (authentication.getPrincipal() instanceof AuthenticatedUser user) {
      return user;
    }

    if (authentication instanceof JwtAuthenticationToken token) {
      Set<String> roles =
          token.getAuthorities().stream()
              .map(GrantedAuthority::getAuthority)
              .map(authority -> authority.startsWith("ROLE_") ? authority.substring(5) : authority)
              .collect(Collectors.toSet());
      return new AuthenticatedUser(
          token.getToken().getSubject(),
          token.getToken().getClaimAsString("email"),
          token.getToken().getClaimAsString("name"),
          roles);
    }

    return null;
  }
}
