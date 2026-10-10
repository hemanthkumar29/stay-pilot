package com.staypilot.resident;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResidentRepository extends JpaRepository<Resident, UUID> {

    Page<Resident> findByOrganizationId(UUID organizationId, Pageable pageable);

    Page<Resident> findByOrganizationIdAndStatus(UUID organizationId, Resident.ResidentStatus status, Pageable pageable);

    Optional<Resident> findByIdAndOrganizationId(UUID id, UUID organizationId);

    long countByOrganizationIdAndStatus(UUID organizationId, Resident.ResidentStatus status);
}
