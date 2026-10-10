package com.staypilot.property;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {

    Page<Property> findByOrganizationIdAndStatusNot(UUID organizationId, PropertyStatus status, Pageable pageable);

    Page<Property> findByOrganizationId(UUID organizationId, Pageable pageable);

    Optional<Property> findByIdAndOrganizationId(UUID id, UUID organizationId);

    long countByOrganizationIdAndStatusNot(UUID organizationId, PropertyStatus status);
}
