package com.staypilot.organization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    @Query("SELECT m FROM Membership m JOIN FETCH m.organization WHERE m.user.id = :userId AND m.status = 'ACTIVE'")
    List<Membership> findActiveByUserId(@Param("userId") UUID userId);

    @Query("SELECT m FROM Membership m WHERE m.user.id = :userId AND m.organization.id = :orgId AND m.status = 'ACTIVE'")
    Optional<Membership> findActiveByUserAndOrg(@Param("userId") UUID userId, @Param("orgId") UUID orgId);

    @Query("SELECT m FROM Membership m JOIN FETCH m.user WHERE m.organization.id = :orgId AND m.status = 'ACTIVE'")
    List<Membership> findActiveByOrganizationId(@Param("orgId") UUID orgId);

    boolean existsByUserIdAndOrganizationId(UUID userId, UUID organizationId);
}
