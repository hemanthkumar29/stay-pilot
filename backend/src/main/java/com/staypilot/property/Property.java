package com.staypilot.property;

import com.staypilot.common.BaseEntity;
import com.staypilot.organization.Organization;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    private String name;

    private String address;
    private String city;
    private String state;
    private String pincode;
    private String phone;
    private String email;

    @Column(nullable = false)
    @Builder.Default
    private String timezone = "Asia/Kolkata";

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PropertyStatus status = PropertyStatus.ACTIVE;

    @Column(name = "rent_due_day")
    @Builder.Default
    private Integer rentDueDay = 1;

    @Column(name = "grace_period_days")
    @Builder.Default
    private Integer gracePeriodDays = 5;
}
