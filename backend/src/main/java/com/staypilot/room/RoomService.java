package com.staypilot.room;

import com.staypilot.bed.Bed;
import com.staypilot.bed.BedDtos;
import com.staypilot.bed.BedRepository;
import com.staypilot.common.Exceptions;
import com.staypilot.property.Property;
import com.staypilot.property.PropertyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final BedRepository bedRepository;
    private final PropertyService propertyService;

    @Transactional
    public RoomDtos.Response create(UUID orgId, UUID propertyId, RoomDtos.CreateRequest request) {
        Property property = propertyService.findByIdAndOrg(propertyId, orgId);

        if (roomRepository.existsByPropertyIdAndRoomNumber(propertyId, request.getRoomNumber())) {
            throw new Exceptions.ConflictException("Room number '" + request.getRoomNumber() + "' already exists in this property");
        }

        Room room = Room.builder()
                .property(property)
                .roomNumber(request.getRoomNumber().trim())
                .floor(request.getFloor() != null ? request.getFloor() : 0)
                .roomType(request.getRoomType() != null
                        ? Room.RoomType.valueOf(request.getRoomType())
                        : Room.RoomType.STANDARD)
                .sharingType(Room.SharingType.valueOf(request.getSharingType()))
                .build();

        room = roomRepository.save(room);
        log.info("Created room '{}' in property {}", room.getRoomNumber(), propertyId);
        return toResponse(room);
    }

    @Transactional(readOnly = true)
    public Page<RoomDtos.Response> listByProperty(UUID orgId, UUID propertyId, Pageable pageable) {
        // Verify property belongs to org
        propertyService.findByIdAndOrg(propertyId, orgId);
        return roomRepository.findByPropertyId(propertyId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public RoomDtos.Response getById(UUID orgId, UUID roomId) {
        Room room = roomRepository.findByIdAndOrganizationId(roomId, orgId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Room not found"));
        return toResponse(room);
    }

    @Transactional
    public BedDtos.Response addBed(UUID orgId, UUID roomId, BedDtos.CreateRequest request) {
        Room room = roomRepository.findByIdAndOrganizationId(roomId, orgId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Room not found"));

        Bed bed = Bed.builder()
                .room(room)
                .bedLabel(request.getBedLabel().trim())
                .monthlyRent(request.getMonthlyRent())
                .build();

        bed = bedRepository.save(bed);
        log.info("Added bed '{}' to room {} ({})", bed.getBedLabel(), room.getRoomNumber(), roomId);

        return BedDtos.Response.builder()
                .id(bed.getId())
                .roomId(room.getId())
                .bedLabel(bed.getBedLabel())
                .status(bed.getStatus().name())
                .monthlyRent(bed.getMonthlyRent())
                .roomNumber(room.getRoomNumber())
                .createdAt(bed.getCreatedAt().toString())
                .build();
    }

    @Transactional(readOnly = true)
    public List<BedDtos.Response> listBeds(UUID orgId, UUID roomId) {
        Room room = roomRepository.findByIdAndOrganizationId(roomId, orgId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("Room not found"));

        return bedRepository.findByRoomId(roomId).stream()
                .map(bed -> BedDtos.Response.builder()
                        .id(bed.getId())
                        .roomId(room.getId())
                        .bedLabel(bed.getBedLabel())
                        .status(bed.getStatus().name())
                        .monthlyRent(bed.getMonthlyRent())
                        .roomNumber(room.getRoomNumber())
                        .createdAt(bed.getCreatedAt().toString())
                        .build())
                .toList();
    }

    private RoomDtos.Response toResponse(Room room) {
        List<Bed> beds = bedRepository.findByRoomId(room.getId());
        int available = (int) beds.stream().filter(b -> b.getStatus() == Bed.BedStatus.AVAILABLE).count();
        int occupied = (int) beds.stream().filter(b -> b.getStatus() == Bed.BedStatus.OCCUPIED).count();

        return RoomDtos.Response.builder()
                .id(room.getId())
                .propertyId(room.getProperty().getId())
                .roomNumber(room.getRoomNumber())
                .floor(room.getFloor())
                .roomType(room.getRoomType().name())
                .sharingType(room.getSharingType().name())
                .status(room.getStatus().name())
                .description(room.getDescription())
                .totalBeds(beds.size())
                .availableBeds(available)
                .occupiedBeds(occupied)
                .createdAt(room.getCreatedAt().toString())
                .build();
    }
}
