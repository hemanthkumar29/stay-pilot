package com.staypilot.resident;

import jakarta.validation.constraints.*;
import lombok.*;
import java.util.UUID;

public class ResidentDtos {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "First name is required")
        @Size(max = 100)
        private String firstName;

        @Size(max = 100)
        private String lastName;

        @Email
        private String email;

        @NotBlank(message = "Phone is required")
        @Size(max = 20)
        private String phone;

        private String emergencyContactName;
        private String emergencyContactPhone;
        private String idProofType;
        private String idProofNumber;
        private String permanentAddress;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private UUID id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String emergencyContactName;
        private String emergencyContactPhone;
        private String status;
        private String createdAt;
    }
}
