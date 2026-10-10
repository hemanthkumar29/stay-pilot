package com.staypilot.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {
    private int status;
    private String code;
    private String message;
    private List<FieldError> fieldErrors;
    private Instant timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
    }

    public static ApiErrorResponse of(int status, String code, String message) {
        return ApiErrorResponse.builder()
                .status(status)
                .code(code)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }
}
