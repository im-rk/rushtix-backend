package com.rushtix.core.domain.entities;

import com.rushtix.core.domain.enums.PricingAlgorithm;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ticket_categories", indexes = {
    @Index(name = "idx_ticket_category_event", columnList = "event_id"),
    @Index(name = "idx_ticket_category_display_order", columnList = "event_id, display_order")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TicketCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    // --- RELATIONSHIPS ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @ToString.Exclude
    private Event event;

    // --- COLUMNS ---

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "current_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "min_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal maxPrice;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "seats_sold", nullable = false)
    private int seatsSold = 0;

    @Column(name = "seats_locked", nullable = false)
    private int seatsLocked = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_algorithm", nullable = false, length = 50)
    private PricingAlgorithm pricingAlgorithm;

    @Column(name = "dynamic_pricing_enabled", nullable = false)
    private boolean dynamicPricingEnabled = false;

    // Optimistic locking for when the AI and a user try to update the price at the same time
    @Version
    @Column(nullable = false)
    private int version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
