package com.staypilot.occupancy;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class OccupancyDtos {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AssignRequest {
        @NotNull(message = "Resident ID is required")
        private UUID residentId;

        @NotNull(message = "Bed ID is required")
        private UUID bedId;

        @NotNull(message = "Agreed rent is required")
        @DecimalMin(value = "0.01")
        private BigDecimal agreedRent;

        @NotNull(message = "Move-in date is required")
        private LocalDate moveInDate;

        private LocalDate expectedMoveOutDate;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class MoveOutRequest {
        @NotNull(message = "Actual move-out date is required")
        private LocalDate actualMoveOutDate;

        private String reason;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private UUID id;
        private UUID residentId;
        private String residentName;
        private UUID bedId;
        private String bedLabel;
        private String roomNumber;
        private String propertyName;
        private BigDecimal agreedRent;
        private LocalDate moveInDate;
        private LocalDate expectedMoveOutDate;
        private LocalDate actualMoveOutDate;
        private String status;
        private String moveOutReason;
        private String createdAt;
    }
}
