package com.staypilot.resident;

import com.staypilot.common.Exceptions;
import com.staypilot.organization.Organization;
import com.staypilot.organization.OrganizationRepository;
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
public class ResidentService {

    private final ResidentRepository residentRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional
    public ResidentDtos.Response create(UUID orgId, ResidentDtos.CreateRequest request) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Organization not found"));

        Resident resident = Resident.builder()
                .organization(org)
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName() != null ? request.getLastName().trim() : null)
                .email(request.getEmail())
                .phone(request.getPhone().trim())
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .idProofType(request.getIdProofType())
                .idProofNumber(request.getIdProofNumber())
                .permanentAddress(request.getPermanentAddress())
                .build();

        resident = residentRepository.save(resident);
        log.info("Created resident '{}' in org {}", resident.getFullName(), orgId);
        return toResponse(resident);
    }

    @Transactional(readOnly = true)
    public Page<ResidentDtos.Response> list(UUID orgId, String status, Pageable pageable) {
        if (status != null && !status.isBlank()) {
            return residentRepository
                    .findByOrganizationIdAndStatus(orgId, Resident.ResidentStatus.valueOf(status), pageable)
                    .map(this::toResponse);
        }
        return residentRepository.findByOrganizationId(orgId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ResidentDtos.Response getById(UUID orgId, UUID residentId) {
        Resident resident = findByIdAndOrg(residentId, orgId);
        return toResponse(resident);
    }

    public Resident findByIdAndOrg(UUID residentId, UUID orgId) {
        return residentRepository.findByIdAndOrganizationId(residentId, orgId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Resident not found"));
    }

    private ResidentDtos.Response toResponse(Resident r) {
        return ResidentDtos.Response.builder()
                .id(r.getId())
                .firstName(r.getFirstName())
                .lastName(r.getLastName())
                .email(r.getEmail())
                .phone(r.getPhone())
                .emergencyContactName(r.getEmergencyContactName())
                .emergencyContactPhone(r.getEmergencyContactPhone())
                .status(r.getStatus().name())
                .createdAt(r.getCreatedAt().toString())
                .build();
    }
}
