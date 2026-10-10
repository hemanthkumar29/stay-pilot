package com.staypilot.security;

import com.staypilot.organization.Membership;
import com.staypilot.organization.MembershipRepository;
import com.staypilot.user.User;
import com.staypilot.user.UserRepository;
import com.staypilot.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        List<Membership> memberships = membershipRepository.findActiveByUserId(user.getId());

        List<UserPrincipal.MembershipInfo> membershipInfos = memberships.stream()
                .map(m -> new UserPrincipal.MembershipInfo(
                        m.getOrganization().getId(),
                        m.getOrganization().getName(),
                        m.getRole()
                ))
                .toList();

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getStatus() == UserStatus.ACTIVE,
                membershipInfos
        );
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(java.util.UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        List<Membership> memberships = membershipRepository.findActiveByUserId(user.getId());

        List<UserPrincipal.MembershipInfo> membershipInfos = memberships.stream()
                .map(m -> new UserPrincipal.MembershipInfo(
                        m.getOrganization().getId(),
                        m.getOrganization().getName(),
                        m.getRole()
                ))
                .toList();

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getStatus() == UserStatus.ACTIVE,
                membershipInfos
        );
    }
}
