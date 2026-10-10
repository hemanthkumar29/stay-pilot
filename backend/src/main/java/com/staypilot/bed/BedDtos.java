package com.staypilot.bed;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

public class BedDtos {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Bed label is required")
        @Size(max = 50)
        private String bedLabel;

        @NotNull(message = "Monthly rent is required")
        @DecimalMin(value = "0.00")
        private BigDecimal monthlyRent;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private UUID id;
        private UUID roomId;
        private String bedLabel;
        private String status;
        private BigDecimal monthlyRent;
        private String roomNumber;
        private String propertyName;
        private String createdAt;
    }
}
