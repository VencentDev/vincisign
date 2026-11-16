package com.vencentdev.backend.modules.organization.repository;

import com.vencentdev.backend.modules.organization.entity.Organization;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {}
