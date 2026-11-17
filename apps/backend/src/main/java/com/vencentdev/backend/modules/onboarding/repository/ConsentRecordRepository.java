package com.vencentdev.backend.modules.onboarding.repository;

import com.vencentdev.backend.modules.onboarding.entity.ConsentRecord;
import com.vencentdev.backend.modules.onboarding.enums.OnboardingConsentType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, UUID> {
  Optional<ConsentRecord> findTopByUserIdAndConsentTypeOrderByCreatedAtDesc(
      UUID userId, OnboardingConsentType consentType);
}
