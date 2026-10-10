package com.staypilot.security;

import com.staypilot.organization.Membership;
import com.staypilot.organization.MembershipRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class UserPrincipal implements UserDetails {

    private final UUID userId;
    private final String email;
    private final String passwordHash;
    private final boolean active;
    private final List<MembershipInfo> memberships;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(UUID userId, String email, String passwordHash, boolean active,
                         List<MembershipInfo> memberships) {
        this.userId = userId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.active = active;
        this.memberships = memberships;

        // Derive authorities from memberships
        Set<GrantedAuthority> auths = new HashSet<>();
        for (MembershipInfo m : memberships) {
            auths.add(new SimpleGrantedAuthority("ROLE_" + m.role().name()));
        }
        this.authorities = auths;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return active;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    /**
     * Get the user's active organization IDs.
     */
    public Set<UUID> getOrganizationIds() {
        return memberships.stream()
                .map(MembershipInfo::organizationId)
                .collect(Collectors.toSet());
    }

    /**
     * Check if the user has a specific role in an organization.
     */
    public boolean hasRoleInOrg(UUID orgId, MembershipRole... roles) {
        Set<MembershipRole> roleSet = Set.of(roles);
        return memberships.stream()
                .anyMatch(m -> m.organizationId().equals(orgId) && roleSet.contains(m.role()));
    }

    /**
     * Get the first (default) organization ID, or empty if none.
     */
    public Optional<UUID> getDefaultOrganizationId() {
        return memberships.stream()
                .findFirst()
                .map(MembershipInfo::organizationId);
    }

    public record MembershipInfo(UUID organizationId, String organizationName, MembershipRole role) {}
}
