package com.staypilot.occupancy;

import com.staypilot.bed.Bed;
import com.staypilot.common.BaseEntity;
import com.staypilot.organization.Organization;
import com.staypilot.resident.Resident;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "occupancy_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OccupancyAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bed_id", nullable = false)
    private Bed bed;

    @Column(name = "agreed_rent", nullable = false, precision = 15, scale = 2)
    private BigDecimal agreedRent;

    @Column(name = "move_in_date", nullable = false)
    private LocalDate moveInDate;

    @Column(name = "expected_move_out_date")
    private LocalDate expectedMoveOutDate;

    @Column(name = "actual_move_out_date")
    private LocalDate actualMoveOutDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private OccupancyStatus status = OccupancyStatus.ACTIVE;

    @Column(name = "move_out_reason")
    private String moveOutReason;

    public enum OccupancyStatus { ACTIVE, MOVED_OUT, TRANSFERRED }
}
