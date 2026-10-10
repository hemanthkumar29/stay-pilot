package com.staypilot.occupancy;

import com.staypilot.bed.Bed;
import com.staypilot.bed.BedRepository;
import com.staypilot.common.Exceptions;
import com.staypilot.organization.Organization;
import com.staypilot.organization.OrganizationRepository;
import com.staypilot.resident.Resident;
import com.staypilot.resident.ResidentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class OccupancyService {

    private final OccupancyRepository occupancyRepository;
    private final BedRepository bedRepository;
    private final ResidentService residentService;
    private final OrganizationRepository organizationRepository;

    /**
     * Assign a resident to a bed.
     * Uses the DB unique partial index on (bed_id) WHERE status='ACTIVE'
     * to prevent double-booking even under concurrent requests.
     */
    @Transactional
    public OccupancyDtos.Response assignBed(UUID orgId, OccupancyDtos.AssignRequest request) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Organization not found"));

        // Verify bed belongs to this org
        Bed bed = bedRepository.findByIdAndOrganizationId(request.getBedId(), orgId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Bed not found"));

        // Check bed availability
        if (bed.getStatus() != Bed.BedStatus.AVAILABLE) {
            throw new Exceptions.ConflictException("Bed is not available. Current status: " + bed.getStatus());
        }

        // Verify resident belongs to this org
        Resident resident = residentService.findByIdAndOrg(request.getResidentId(), orgId);

        // Check resident doesn't already have an active occupancy
        occupancyRepository.findActiveByResidentId(resident.getId()).ifPresent(existing -> {
            throw new Exceptions.ConflictException("Resident already has an active bed assignment");
        });

        // Create occupancy assignment
        OccupancyAssignment assignment = OccupancyAssignment.builder()
                .organization(org)
                .resident(resident)
                .bed(bed)
                .agreedRent(request.getAgreedRent())
                .moveInDate(request.getMoveInDate())
                .expectedMoveOutDate(request.getExpectedMoveOutDate())
                .status(OccupancyAssignment.OccupancyStatus.ACTIVE)
                .build();

        // Update bed status
        bed.setStatus(Bed.BedStatus.OCCUPIED);
        bedRepository.save(bed);

        // Save occupancy — the unique partial index enforces no double-booking
        try {
            assignment = occupancyRepository.save(assignment);
        } catch (Exception e) {
            // If concurrent insert happened, the unique index will throw
            throw new Exceptions.ConflictException("Bed was just assigned to another resident. Please try another bed.");
        }

        log.info("Assigned resident {} to bed {} in org {}", resident.getId(), bed.getId(), orgId);
        return toResponse(assignment);
    }

    @Transactional
    public OccupancyDtos.Response moveOut(UUID orgId, UUID occupancyId, OccupancyDtos.MoveOutRequest request) {
        OccupancyAssignment assignment = occupancyRepository.findByIdAndOrganizationId(occupancyId, orgId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Occupancy assignment not found"));

        if (assignment.getStatus() != OccupancyAssignment.OccupancyStatus.ACTIVE) {
            throw new Exceptions.BusinessRuleException("Only active occupancies can be moved out");
        }

        assignment.setStatus(OccupancyAssignment.OccupancyStatus.MOVED_OUT);
        assignment.setActualMoveOutDate(request.getActualMoveOutDate());
        assignment.setMoveOutReason(request.getReason());

        // Release the bed
        Bed bed = assignment.getBed();
        bed.setStatus(Bed.BedStatus.AVAILABLE);
        bedRepository.save(bed);

        // Update resident status
        Resident resident = assignment.getResident();
        resident.setStatus(Resident.ResidentStatus.MOVED_OUT);

        assignment = occupancyRepository.save(assignment);
        log.info("Move-out: resident {} from bed {} in org {}", resident.getId(), bed.getId(), orgId);
        return toResponse(assignment);
    }

    @Transactional(readOnly = true)
    public Page<OccupancyDtos.Response> list(UUID orgId, Pageable pageable) {
        return occupancyRepository.findByOrganizationId(orgId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public OccupancyDtos.Response getById(UUID orgId, UUID occupancyId) {
        OccupancyAssignment assignment = occupancyRepository.findByIdAndOrganizationId(occupancyId, orgId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Occupancy assignment not found"));
        return toResponse(assignment);
    }

    private OccupancyDtos.Response toResponse(OccupancyAssignment a) {
        return OccupancyDtos.Response.builder()
                .id(a.getId())
                .residentId(a.getResident().getId())
                .residentName(a.getResident().getFullName())
                .bedId(a.getBed().getId())
                .bedLabel(a.getBed().getBedLabel())
                .roomNumber(a.getBed().getRoom().getRoomNumber())
                .propertyName(a.getBed().getRoom().getProperty().getName())
                .agreedRent(a.getAgreedRent())
                .moveInDate(a.getMoveInDate())
                .expectedMoveOutDate(a.getExpectedMoveOutDate())
                .actualMoveOutDate(a.getActualMoveOutDate())
                .status(a.getStatus().name())
                .moveOutReason(a.getMoveOutReason())
                .createdAt(a.getCreatedAt().toString())
                .build();
    }
}
