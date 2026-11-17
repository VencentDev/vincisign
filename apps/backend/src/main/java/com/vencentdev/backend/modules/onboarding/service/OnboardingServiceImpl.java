package com.vencentdev.backend.modules.onboarding.service;

import com.vencentdev.backend.common.exception.BadRequestException;
import com.vencentdev.backend.modules.auth.AuthenticatedUser;
import com.vencentdev.backend.modules.onboarding.dto.CompleteOnboardingRequest;
import com.vencentdev.backend.modules.onboarding.dto.ConsentRequirementResponse;
import com.vencentdev.backend.modules.onboarding.dto.OnboardingConsentDecision;
import com.vencentdev.backend.modules.onboarding.dto.OnboardingStatusResponse;
import com.vencentdev.backend.modules.onboarding.entity.ConsentRecord;
import com.vencentdev.backend.modules.onboarding.enums.ConsentStatus;
import com.vencentdev.backend.modules.onboarding.enums.OnboardingConsentType;
import com.vencentdev.backend.modules.onboarding.repository.ConsentRecordRepository;
import com.vencentdev.backend.modules.user.entity.User;
import com.vencentdev.backend.modules.user.enums.UserType;
import com.vencentdev.backend.modules.user.repository.UserRepository;
import com.vencentdev.backend.modules.user.service.UserService;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OnboardingServiceImpl implements OnboardingService {

  private static final List<String> PLATFORM_LIMITS =
      List.of(
          "VinciSign is a document workflow and evidence platform, not a legal-advice service.",
          "Ordinary signing and electronic notarization use separate controls and eligibility checks.",
          "A consent record preserves what was shown and accepted; it does not independently determine legal validity or identity.");

  private final ConsentRecordRepository consentRecords;
  private final UserRepository users;
  private final UserService userService;

  public OnboardingServiceImpl(
      ConsentRecordRepository consentRecords, UserRepository users, UserService userService) {
    this.consentRecords = consentRecords;
    this.users = users;
    this.userService = userService;
  }

  @Override
  @Transactional(readOnly = true)
  public OnboardingStatusResponse status(AuthenticatedUser principal) {
    return toStatus(findUser(principal));
  }

  @Override
  @Transactional
  public OnboardingStatusResponse complete(
      AuthenticatedUser principal, CompleteOnboardingRequest request) {
    User user = findUser(principal);
    Map<OnboardingConsentType, OnboardingConsentDecision> decisions = decisions(request);
    Instant now = Instant.now();

    user.setUserType(request.userType());
    decisions.forEach((type, decision) -> recordDecision(user, type, decision.accepted(), now));

    OnboardingStatusResponse status = toStatus(user);
    boolean allRequiredConsentsAccepted =
        status.requiredConsents().stream()
            .allMatch(requirement -> requirement.status() == ConsentStatus.ACCEPTED);
    user.setOnboardingCompletedAt(allRequiredConsentsAccepted ? now : null);
    return toStatus(user);
  }

  @Override
  @Transactional
  public OnboardingStatusResponse withdraw(
      AuthenticatedUser principal, OnboardingConsentType consentType) {
    User user = findUser(principal);
    ConsentRecord current =
        consentRecords
            .findTopByUserIdAndConsentTypeOrderByCreatedAtDesc(user.getId(), consentType)
            .orElseThrow(() -> new BadRequestException("No consent has been accepted yet"));
    if (current.getStatus() != ConsentStatus.ACCEPTED) {
      throw new BadRequestException("Only an accepted consent can be withdrawn");
    }

    Instant now = Instant.now();
    consentRecords.save(
        ConsentRecord.builder()
            .userId(user.getId())
            .consentType(consentType)
            .policyId(current.getPolicyId())
            .policyVersion(current.getPolicyVersion())
            .policyReference(current.getPolicyReference())
            .consentText(current.getConsentText())
            .locale(current.getLocale())
            .status(ConsentStatus.WITHDRAWN)
            .decidedAt(now)
            .withdrawnAt(now)
            .actorUserId(user.getId())
            .build());
    user.setOnboardingCompletedAt(null);
    return toStatus(user);
  }

  private User findUser(AuthenticatedUser principal) {
    UUID userId = userService.resolveInternalId(principal);
    return users
        .findById(userId)
        .orElseThrow(() -> new BadRequestException("Unable to load the current account"));
  }

  private Map<OnboardingConsentType, OnboardingConsentDecision> decisions(
      CompleteOnboardingRequest request) {
    Map<OnboardingConsentType, OnboardingConsentDecision> decisions = new HashMap<>();
    for (OnboardingConsentDecision decision : request.consents()) {
      if (decisions.put(decision.type(), decision) != null) {
        throw new BadRequestException("Each onboarding consent can be submitted only once");
      }
    }
    return decisions;
  }

  private void recordDecision(
      User user, OnboardingConsentType type, boolean accepted, Instant now) {
    OnboardingPolicyCatalog.PolicyDefinition policy = policy(type);
    ConsentStatus status = accepted ? ConsentStatus.ACCEPTED : ConsentStatus.DECLINED;
    ConsentRecord current =
        consentRecords
            .findTopByUserIdAndConsentTypeOrderByCreatedAtDesc(user.getId(), type)
            .orElse(null);
    if (current != null
        && current.getStatus() == status
        && current.getPolicyVersion().equals(policy.policyVersion())) {
      return;
    }

    consentRecords.save(
        ConsentRecord.builder()
            .userId(user.getId())
            .consentType(type)
            .policyId(policy.policyId())
            .policyVersion(policy.policyVersion())
            .policyReference(policy.policyReference())
            .consentText(policy.description())
            .status(status)
            .decidedAt(now)
            .actorUserId(user.getId())
            .build());
  }

  private OnboardingStatusResponse toStatus(User user) {
    List<ConsentRequirementResponse> requirements =
        OnboardingPolicyCatalog.required().stream()
            .map(policy -> toRequirement(user.getId(), policy))
            .toList();
    boolean completed =
        user.getOnboardingCompletedAt() != null
            && requirements.stream()
                .allMatch(requirement -> requirement.status() == ConsentStatus.ACCEPTED);
    String blockedCode =
        completed
            ? null
            : requirements.stream()
                    .anyMatch(
                        requirement ->
                            requirement.status() == ConsentStatus.DECLINED
                                || requirement.status() == ConsentStatus.WITHDRAWN)
                ? "CONSENT_DECLINED"
                : "CONSENT_REQUIRED";
    return new OnboardingStatusResponse(
        completed, user.getUserType(), blockedCode, PLATFORM_LIMITS, requirements);
  }

  private ConsentRequirementResponse toRequirement(
      UUID userId, OnboardingPolicyCatalog.PolicyDefinition policy) {
    ConsentRecord current =
        consentRecords
            .findTopByUserIdAndConsentTypeOrderByCreatedAtDesc(userId, policy.type())
            .orElse(null);
    ConsentStatus currentStatus =
        current != null && current.getPolicyVersion().equals(policy.policyVersion())
            ? current.getStatus()
            : null;
    return new ConsentRequirementResponse(
        policy.type(),
        policy.policyId(),
        policy.policyVersion(),
        policy.policyReference(),
        policy.title(),
        policy.description(),
        true,
        currentStatus,
        currentStatus == ConsentStatus.ACCEPTED
            ? current.getDecidedAt()
            : null);
  }

  private OnboardingPolicyCatalog.PolicyDefinition policy(OnboardingConsentType type) {
    return OnboardingPolicyCatalog.required().stream()
        .filter(candidate -> candidate.type() == type)
        .findFirst()
        .orElseThrow(() -> new BadRequestException("Unsupported onboarding consent type"));
  }
}
