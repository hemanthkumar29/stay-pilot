package com.staypilot.room;

import com.staypilot.common.BaseEntity;
import com.staypilot.property.Property;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rooms", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"property_id", "room_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(name = "room_number", nullable = false)
    private String roomNumber;

    @Column
    @Builder.Default
    private Integer floor = 0;

    @Column(name = "room_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RoomType roomType = RoomType.STANDARD;

    @Column(name = "sharing_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SharingType sharingType = SharingType.SINGLE;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RoomStatus status = RoomStatus.ACTIVE;

    @Column
    private String description;

    public enum RoomType { STANDARD, DELUXE, PREMIUM }
    public enum SharingType { SINGLE, DOUBLE, TRIPLE, QUAD, DORMITORY }
    public enum RoomStatus { ACTIVE, INACTIVE, MAINTENANCE }
}
