package com.staypilot.bed;

import com.staypilot.common.BaseEntity;
import com.staypilot.room.Room;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "beds", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"room_id", "bed_label"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bed extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "bed_label", nullable = false)
    private String bedLabel;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BedStatus status = BedStatus.AVAILABLE;

    @Column(name = "monthly_rent", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal monthlyRent = BigDecimal.ZERO;

    public enum BedStatus {
        AVAILABLE,
        RESERVED,
        OCCUPIED,
        MAINTENANCE_BLOCKED,
        INACTIVE
    }
}
