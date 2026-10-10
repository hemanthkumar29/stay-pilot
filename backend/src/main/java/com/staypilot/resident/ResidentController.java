package com.staypilot.resident;

import com.staypilot.common.Exceptions;
import com.staypilot.security.UserPrincipal;
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
@RequestMapping("/api/v1/residents")
@RequiredArgsConstructor
public class ResidentController {

    private final ResidentService residentService;

    @PostMapping
    public ResponseEntity<ResidentDtos.Response> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ResidentDtos.CreateRequest request) {
        UUID orgId = resolveOrgId(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(residentService.create(orgId, request));
    }

    @GetMapping
    public ResponseEntity<Page<ResidentDtos.Response>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        UUID orgId = resolveOrgId(principal);
        return ResponseEntity.ok(residentService.list(orgId, status, pageable));
    }

    @GetMapping("/{residentId}")
    public ResponseEntity<ResidentDtos.Response> getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID residentId) {
        UUID orgId = resolveOrgId(principal);
        return ResponseEntity.ok(residentService.getById(orgId, residentId));
    }

    private UUID resolveOrgId(UserPrincipal principal) {
        return principal.getDefaultOrganizationId()
                .orElseThrow(() -> new Exceptions.ForbiddenException("No organization membership"));
    }
}
