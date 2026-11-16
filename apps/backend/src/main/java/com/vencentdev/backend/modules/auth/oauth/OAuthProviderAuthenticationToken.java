package com.vencentdev.backend.modules.auth.oauth;

import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import java.util.List;
import java.util.Set;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class OAuthProviderAuthenticationToken extends AbstractAuthenticationToken {

  private final AuthenticatedUser principal;
  private final String credentials;

  public OAuthProviderAuthenticationToken(AuthenticatedUser principal, String credentials) {
    super(authorities(principal.roles()));
    this.principal = principal;
    this.credentials = credentials;
    setAuthenticated(true);
  }

  @Override
  public String getCredentials() {
    return credentials;
  }

  @Override
  public AuthenticatedUser getPrincipal() {
    return principal;
  }

  @Override
  public String getName() {
    return principal.subject();
  }

  private static List<GrantedAuthority> authorities(Set<String> roles) {
    return roles.stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
        .map(GrantedAuthority.class::cast)
        .toList();
  }
}
