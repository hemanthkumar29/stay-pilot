package com.staypilot.security;

import com.staypilot.common.Exceptions;
import com.staypilot.organization.*;
import com.staypilot.user.User;
import com.staypilot.user.UserRepository;
import com.staypilot.user.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final MembershipRepository membershipRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final JwtProperties jwtProperties;

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new Exceptions.ConflictException("An account with this email already exists");
        }

        // Create user
        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName() != null ? request.getLastName().trim() : null)
                .phone(request.getPhone())
                .status(UserStatus.ACTIVE)
                .build();
        user = userRepository.save(user);

        // Create organization
        String slug = generateSlug(request.getOrganizationName());
        Organization org = Organization.builder()
                .name(request.getOrganizationName().trim())
                .slug(slug)
                .status(OrganizationStatus.ACTIVE)
                .country("India")
                .build();
        org = organizationRepository.save(org);

        // Create membership as owner
        Membership membership = Membership.builder()
                .user(user)
                .organization(org)
                .role(MembershipRole.OWNER)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(Instant.now())
                .build();
        membershipRepository.save(membership);

        log.info("New registration: user={}, org={}", user.getEmail(), org.getName());

        // Generate tokens
        return buildAuthResponse(user, List.of(membership));
    }

    @Transactional
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase().trim(),
                            request.getPassword()
                    )
            );

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

            // Update last login
            User user = userRepository.findById(principal.getUserId())
                    .orElseThrow(() -> new Exceptions.UnauthorizedException("User not found"));
            user.setLastLoginAt(Instant.now());
            userRepository.save(user);

            List<Membership> memberships = membershipRepository.findActiveByUserId(user.getId());
            return buildAuthResponse(user, memberships);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    public AuthDtos.AuthResponse refreshToken(AuthDtos.RefreshTokenRequest request) {
        String token = request.getRefreshToken();

        if (!jwtService.validateToken(token)) {
            throw new Exceptions.UnauthorizedException("Invalid or expired refresh token");
        }

        String tokenType = jwtService.getTokenType(token);
        if (!"refresh".equals(tokenType)) {
            throw new Exceptions.UnauthorizedException("Invalid token type");
        }

        UUID userId = jwtService.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exceptions.UnauthorizedException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new Exceptions.UnauthorizedException("Account is not active");
        }

        List<Membership> memberships = membershipRepository.findActiveByUserId(user.getId());
        return buildAuthResponse(user, memberships);
    }

    @Transactional(readOnly = true)
    public AuthDtos.UserInfo getCurrentUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new Exceptions.ResourceNotFoundException("User not found"));

        List<Membership> memberships = membershipRepository.findActiveByUserId(userId);

        return AuthDtos.UserInfo.builder()
                .id(user.getId().toString())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .organizations(memberships.stream()
                        .map(m -> AuthDtos.OrgMembership.builder()
                                .organizationId(m.getOrganization().getId().toString())
                                .organizationName(m.getOrganization().getName())
                                .role(m.getRole().name())
                                .build())
                        .toList())
                .build();
    }

    private AuthDtos.AuthResponse buildAuthResponse(User user, List<Membership> memberships) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        return AuthDtos.AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpiryMs() / 1000)
                .user(AuthDtos.UserInfo.builder()
                        .id(user.getId().toString())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .phone(user.getPhone())
                        .organizations(memberships.stream()
                                .map(m -> AuthDtos.OrgMembership.builder()
                                        .organizationId(m.getOrganization().getId().toString())
                                        .organizationName(m.getOrganization().getName())
                                        .role(m.getRole().name())
                                        .build())
                                .toList())
                        .build())
                .build();
    }

    private String generateSlug(String name) {
        String base = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        if (base.isEmpty()) {
            base = "org";
        }

        String slug = base;
        int counter = 1;
        while (organizationRepository.existsBySlug(slug)) {
            slug = base + "-" + counter;
            counter++;
        }
        return slug;
    }
}
