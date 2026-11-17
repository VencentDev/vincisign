package com.vencentdev.backend.modules.auth.session;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthSessionRepository extends JpaRepository<AuthSession, UUID> {
  Optional<AuthSession> findByTokenHash(String tokenHash);

  Optional<AuthSession> findByTokenHashAndRevokedAtIsNull(String tokenHash);
}
