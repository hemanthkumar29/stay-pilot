package com.staypilot.property;

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
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final OrganizationRepository organizationRepository;

    @Transactional
    public PropertyDtos.Response create(UUID organizationId, PropertyDtos.CreateRequest request) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Organization not found"));

        Property property = Property.builder()
                .organization(org)
                .name(request.getName().trim())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .phone(request.getPhone())
                .email(request.getEmail())
                .timezone(request.getTimezone() != null ? request.getTimezone() : "Asia/Kolkata")
                .rentDueDay(request.getRentDueDay() != null ? request.getRentDueDay() : 1)
                .gracePeriodDays(request.getGracePeriodDays() != null ? request.getGracePeriodDays() : 5)
                .build();

        property = propertyRepository.save(property);
        log.info("Created property '{}' for org {}", property.getName(), organizationId);
        return toResponse(property);
    }

    @Transactional(readOnly = true)
    public Page<PropertyDtos.Response> list(UUID organizationId, Pageable pageable) {
        return propertyRepository
                .findByOrganizationIdAndStatusNot(organizationId, PropertyStatus.ARCHIVED, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public PropertyDtos.Response getById(UUID organizationId, UUID propertyId) {
        Property property = findByIdAndOrg(propertyId, organizationId);
        return toResponse(property);
    }

    @Transactional
    public PropertyDtos.Response update(UUID organizationId, UUID propertyId, PropertyDtos.UpdateRequest request) {
        Property property = findByIdAndOrg(propertyId, organizationId);

        if (request.getName() != null) property.setName(request.getName().trim());
        if (request.getAddress() != null) property.setAddress(request.getAddress());
        if (request.getCity() != null) property.setCity(request.getCity());
        if (request.getState() != null) property.setState(request.getState());
        if (request.getPincode() != null) property.setPincode(request.getPincode());
        if (request.getPhone() != null) property.setPhone(request.getPhone());
        if (request.getEmail() != null) property.setEmail(request.getEmail());
        if (request.getTimezone() != null) property.setTimezone(request.getTimezone());
        if (request.getRentDueDay() != null) property.setRentDueDay(request.getRentDueDay());
        if (request.getGracePeriodDays() != null) property.setGracePeriodDays(request.getGracePeriodDays());
        if (request.getStatus() != null) {
            property.setStatus(PropertyStatus.valueOf(request.getStatus()));
        }

        property = propertyRepository.save(property);
        return toResponse(property);
    }

    public Property findByIdAndOrg(UUID propertyId, UUID organizationId) {
        return propertyRepository.findByIdAndOrganizationId(propertyId, organizationId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Property not found"));
    }

    private PropertyDtos.Response toResponse(Property p) {
        return PropertyDtos.Response.builder()
                .id(p.getId())
                .name(p.getName())
                .address(p.getAddress())
                .city(p.getCity())
                .state(p.getState())
                .pincode(p.getPincode())
                .phone(p.getPhone())
                .email(p.getEmail())
                .timezone(p.getTimezone())
                .status(p.getStatus().name())
                .rentDueDay(p.getRentDueDay())
                .gracePeriodDays(p.getGracePeriodDays())
                .createdAt(p.getCreatedAt().toString())
                .updatedAt(p.getUpdatedAt().toString())
                .build();
    }
}
