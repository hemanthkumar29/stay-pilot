package com.staypilot.room;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.UUID;

public class RoomDtos {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Room number is required")
        @Size(max = 50)
        private String roomNumber;

        private Integer floor;

        private String roomType; // STANDARD, DELUXE, PREMIUM

        @NotBlank(message = "Sharing type is required")
        private String sharingType; // SINGLE, DOUBLE, TRIPLE, QUAD, DORMITORY

        private String description;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private UUID id;
        private UUID propertyId;
        private String roomNumber;
        private Integer floor;
        private String roomType;
        private String sharingType;
        private String status;
        private String description;
        private int totalBeds;
        private int availableBeds;
        private int occupiedBeds;
        private String createdAt;
    }
}
