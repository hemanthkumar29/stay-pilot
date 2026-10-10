package com.staypilot.property;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

public class PropertyDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Property name is required")
        @Size(max = 255)
        private String name;

        private String address;

        @Size(max = 100)
        private String city;

        @Size(max = 100)
        private String state;

        @Size(max = 10)
        private String pincode;

        @Size(max = 20)
        private String phone;

        @Email
        private String email;

        private String timezone;

        @Min(value = 1, message = "Rent due day must be between 1 and 28")
        @Max(value = 28, message = "Rent due day must be between 1 and 28")
        private Integer rentDueDay;

        @Min(value = 0)
        private Integer gracePeriodDays;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Size(max = 255)
        private String name;
        private String address;
        private String city;
        private String state;
        private String pincode;
        private String phone;
        @Email
        private String email;
        private String timezone;
        @Min(1) @Max(28)
        private Integer rentDueDay;
        @Min(0)
        private Integer gracePeriodDays;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private UUID id;
        private String name;
        private String address;
        private String city;
        private String state;
        private String pincode;
        private String phone;
        private String email;
        private String timezone;
        private String status;
        private Integer rentDueDay;
        private Integer gracePeriodDays;
        private String createdAt;
        private String updatedAt;
    }
}
