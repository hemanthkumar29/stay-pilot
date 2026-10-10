package com.staypilot.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "staypilot.jwt")
public class JwtProperties {
    private String secret;
    private long accessTokenExpiryMs = 900000; // 15 minutes
    private long refreshTokenExpiryMs = 604800000; // 7 days
}
