package com.vencentdev.backend.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public class AuthSessionProperties {
  private Duration sessionTtl = Duration.ofDays(30);

  public Duration getSessionTtl() {
    return sessionTtl;
  }

  public void setSessionTtl(Duration sessionTtl) {
    this.sessionTtl = sessionTtl;
  }
}
