package com.staypilot.bed;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BedRepository extends JpaRepository<Bed, UUID> {

    List<Bed> findByRoomId(UUID roomId);

    Optional<Bed> findByIdAndRoomId(UUID id, UUID roomId);

    @Query("SELECT b FROM Bed b WHERE b.id = :bedId AND b.room.property.organization.id = :orgId")
    Optional<Bed> findByIdAndOrganizationId(@Param("bedId") UUID bedId, @Param("orgId") UUID orgId);

    @Query("SELECT b FROM Bed b WHERE b.room.property.id = :propertyId")
    List<Bed> findByPropertyId(@Param("propertyId") UUID propertyId);

    @Query("SELECT COUNT(b) FROM Bed b WHERE b.room.property.id = :propertyId AND b.status = :status")
    long countByPropertyIdAndStatus(@Param("propertyId") UUID propertyId, @Param("status") Bed.BedStatus status);

    @Query("SELECT COUNT(b) FROM Bed b WHERE b.room.property.organization.id = :orgId")
    long countByOrganizationId(@Param("orgId") UUID orgId);

    @Query("SELECT COUNT(b) FROM Bed b WHERE b.room.property.organization.id = :orgId AND b.status = :status")
    long countByOrganizationIdAndStatus(@Param("orgId") UUID orgId, @Param("status") Bed.BedStatus status);

    @Query("SELECT b FROM Bed b WHERE b.room.property.id = :propertyId AND b.status = 'AVAILABLE'")
    List<Bed> findAvailableByPropertyId(@Param("propertyId") UUID propertyId);
}
