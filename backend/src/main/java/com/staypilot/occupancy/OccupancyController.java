package com.staypilot.occupancy;

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
@RequestMapping("/api/v1/occupancies")
@RequiredArgsConstructor
public class OccupancyController {

    private final OccupancyService occupancyService;

    @PostMapping
    public ResponseEntity<OccupancyDtos.Response> assign(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody OccupancyDtos.AssignRequest request) {
        UUID orgId = resolveOrgId(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(occupancyService.assignBed(orgId, request));
    }

    @PostMapping("/{occupancyId}/move-out")
    public ResponseEntity<OccupancyDtos.Response> moveOut(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID occupancyId,
            @Valid @RequestBody OccupancyDtos.MoveOutRequest request) {
        UUID orgId = resolveOrgId(principal);
        return ResponseEntity.ok(occupancyService.moveOut(orgId, occupancyId, request));
    }

    @GetMapping
    public ResponseEntity<Page<OccupancyDtos.Response>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {
        UUID orgId = resolveOrgId(principal);
        return ResponseEntity.ok(occupancyService.list(orgId, pageable));
    }

    @GetMapping("/{occupancyId}")
    public ResponseEntity<OccupancyDtos.Response> getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID occupancyId) {
        UUID orgId = resolveOrgId(principal);
        return ResponseEntity.ok(occupancyService.getById(orgId, occupancyId));
    }

    private UUID resolveOrgId(UserPrincipal principal) {
        return principal.getDefaultOrganizationId()
                .orElseThrow(() -> new Exceptions.ForbiddenException("No organization membership"));
    }
}
