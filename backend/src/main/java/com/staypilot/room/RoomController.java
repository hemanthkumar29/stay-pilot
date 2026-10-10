package com.staypilot.room;

import com.staypilot.bed.BedDtos;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/properties/{propertyId}/rooms")
    public ResponseEntity<RoomDtos.Response> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID propertyId,
            @Valid @RequestBody RoomDtos.CreateRequest request) {
        UUID orgId = resolveOrgId(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roomService.create(orgId, propertyId, request));
    }

    @GetMapping("/properties/{propertyId}/rooms")
    public ResponseEntity<Page<RoomDtos.Response>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID propertyId,
            Pageable pageable) {
        UUID orgId = resolveOrgId(principal);
        return ResponseEntity.ok(roomService.listByProperty(orgId, propertyId, pageable));
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<RoomDtos.Response> getById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID roomId) {
        UUID orgId = resolveOrgId(principal);
        return ResponseEntity.ok(roomService.getById(orgId, roomId));
    }

    @PostMapping("/rooms/{roomId}/beds")
    public ResponseEntity<BedDtos.Response> addBed(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID roomId,
            @Valid @RequestBody BedDtos.CreateRequest request) {
        UUID orgId = resolveOrgId(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roomService.addBed(orgId, roomId, request));
    }

    @GetMapping("/rooms/{roomId}/beds")
    public ResponseEntity<List<BedDtos.Response>> listBeds(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID roomId) {
        UUID orgId = resolveOrgId(principal);
        return ResponseEntity.ok(roomService.listBeds(orgId, roomId));
    }

    private UUID resolveOrgId(UserPrincipal principal) {
        return principal.getDefaultOrganizationId()
                .orElseThrow(() -> new Exceptions.ForbiddenException("No organization membership"));
    }
}
