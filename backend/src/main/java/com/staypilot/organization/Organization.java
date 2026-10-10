package com.staypilot.organization;

import com.staypilot.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OrganizationStatus status = OrganizationStatus.ACTIVE;

    private String phone;
    private String email;
    private String address;
    private String city;
    private String state;
    private String country;
    private String pincode;
}
