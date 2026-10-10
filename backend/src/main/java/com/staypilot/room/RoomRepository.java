package com.staypilot.room;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    Page<Room> findByPropertyId(UUID propertyId, Pageable pageable);

    Optional<Room> findByIdAndPropertyId(UUID id, UUID propertyId);

    boolean existsByPropertyIdAndRoomNumber(UUID propertyId, String roomNumber);

    @Query("SELECT r FROM Room r WHERE r.id = :roomId AND r.property.organization.id = :orgId")
    Optional<Room> findByIdAndOrganizationId(@Param("roomId") UUID roomId, @Param("orgId") UUID orgId);
}
