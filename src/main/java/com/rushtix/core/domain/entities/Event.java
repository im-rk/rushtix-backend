package com.rushtix.core.domain.entities;

import com.rushtix.core.domain.enums.EventStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "events", indexes = {
    @Index(name = "idx_event_status_date", columnList = "status, event_date"),
    @Index(name = "idx_event_organizer", columnList = "organizer_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    // --- RELATIONSHIPS ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    @ToString.Exclude
    private User organizer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    @ToString.Exclude
    private Venue venue;

    // --- COLUMNS ---

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "event_date", nullable = false)
    private OffsetDateTime eventDate;

    @Column(name = "event_end_date")
    private OffsetDateTime eventEndDate;

    @Column(name = "booking_opens_at", nullable = false)
    private OffsetDateTime bookingOpensAt;

    @Column(name = "booking_closes_at", nullable = false)
    private OffsetDateTime bookingClosesAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EventStatus status;

    @Column(name = "total_seats", nullable = false)
    private int totalSeats;

    @org.hibernate.annotations.Formula("(SELECT CAST(COALESCE(COUNT(s.id), 0) AS int) FROM seats s WHERE s.event_id = id AND s.status = 'BOOKED')")
    private int seatsSold;

    @org.hibernate.annotations.Formula("(SELECT CAST(COALESCE(COUNT(s.id), 0) AS int) FROM seats s WHERE s.event_id = id AND s.status = 'LOCKED')")
    private int seatsLocked;

    // Optimistic Locking for concurrent booking
    @Version
    @Column(nullable = false)
    private int version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;
}
