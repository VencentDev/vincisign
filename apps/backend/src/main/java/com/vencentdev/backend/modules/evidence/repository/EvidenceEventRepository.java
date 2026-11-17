package com.vencentdev.backend.modules.evidence.repository;

import com.vencentdev.backend.modules.evidence.entity.EvidenceEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenceEventRepository extends JpaRepository<EvidenceEvent, UUID> {

  List<EvidenceEvent> findByProjectIdAndOrganizationIdOrderBySequenceNumberAsc(
      UUID projectId, UUID organizationId);
}
