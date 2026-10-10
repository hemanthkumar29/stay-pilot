package com.staypilot.occupancy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OccupancyRepository extends JpaRepository<OccupancyAssignment, UUID> {

    @Query("SELECT o FROM OccupancyAssignment o WHERE o.bed.id = :bedId AND o.status = 'ACTIVE'")
    Optional<OccupancyAssignment> findActiveByBedId(@Param("bedId") UUID bedId);

    @Query("SELECT o FROM OccupancyAssignment o WHERE o.resident.id = :residentId AND o.status = 'ACTIVE'")
    Optional<OccupancyAssignment> findActiveByResidentId(@Param("residentId") UUID residentId);

    Page<OccupancyAssignment> findByOrganizationId(UUID organizationId, Pageable pageable);

    @Query("SELECT o FROM OccupancyAssignment o WHERE o.organization.id = :orgId AND o.status = 'ACTIVE'")
    List<OccupancyAssignment> findActiveByOrganizationId(@Param("orgId") UUID orgId);

    Optional<OccupancyAssignment> findByIdAndOrganizationId(UUID id, UUID organizationId);

    List<OccupancyAssignment> findByResidentId(UUID residentId);

    @Query("SELECT COUNT(o) FROM OccupancyAssignment o WHERE o.organization.id = :orgId AND o.status = 'ACTIVE'")
    long countActiveByOrganizationId(@Param("orgId") UUID orgId);
}
