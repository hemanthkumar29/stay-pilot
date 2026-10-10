package com.staypilot.resident;

import com.staypilot.common.BaseEntity;
import com.staypilot.organization.Organization;
import com.staypilot.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "residents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resident extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;

    @Column(name = "id_proof_type")
    private String idProofType;

    @Column(name = "id_proof_number")
    private String idProofNumber;

    @Column(name = "permanent_address")
    private String permanentAddress;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ResidentStatus status = ResidentStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public String getFullName() {
        if (lastName == null || lastName.isBlank()) return firstName;
        return firstName + " " + lastName;
    }

    public enum ResidentStatus { ACTIVE, MOVED_OUT, CANCELLED }
}
