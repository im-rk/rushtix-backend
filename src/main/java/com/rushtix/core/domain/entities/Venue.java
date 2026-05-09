package com.rushtix.core.domain.entities;

import com.rushtix.core.domain.enums.VenueStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "venues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(name = "address_line", nullable = false, columnDefinition = "TEXT")
    private String addressLine;

    @Column(length = 10)
    private String pincode;

    // Use BigDecimal for coordinates to maintain precise GPS locations
    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "total_capacity", nullable = false)
    private Integer totalCapacity;

    @Column(nullable = false, length = 50)
    private String timezone;

    // Maps your PostgreSQL JSONB column securely to a Java String
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "seat_map_config", nullable = false, columnDefinition = "jsonb")
    private String seatMapConfig;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VenueStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}