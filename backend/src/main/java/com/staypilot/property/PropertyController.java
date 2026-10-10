package com.staypilot.property;

import com.staypilot.security.UserPrincipal;
import com.staypilot.common.Exceptions;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping
    public ResponseEntity<PropertyDtos.Response> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PropertyDtos.CreateRequest request) {
        UUID orgId = resolveOrgId(principal);
        PropertyDtos.Response response = propertyService.create(orgId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<PropertyDtos.Response>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {
        UUID orgId = resolveOrgId(principal);
        return ResponseEntity.ok(propertyService.list(orgId, pageable));
    }

    @GetMapping("/{propertyId}")
    public ResponseEntity<PropertyDtos.Response> getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID propertyId) {
        UUID orgId = resolveOrgId(principal);
        return ResponseEntity.ok(propertyService.getById(orgId, propertyId));
    }

    @PutMapping("/{propertyId}")
    public ResponseEntity<PropertyDtos.Response> update(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID propertyId,
            @Valid @RequestBody PropertyDtos.UpdateRequest request) {
        UUID orgId = resolveOrgId(principal);
        return ResponseEntity.ok(propertyService.update(orgId, propertyId, request));
    }

    private UUID resolveOrgId(UserPrincipal principal) {
        return principal.getDefaultOrganizationId()
                .orElseThrow(() -> new Exceptions.ForbiddenException("No organization membership"));
    }
}
