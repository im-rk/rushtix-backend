package com.rushtix.core.domain.entities;

import com.rushtix.core.domain.enums.SplitStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "group_payment_items")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupPaymentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "group_booking_id", nullable = false)
    private GroupBooking groupBooking;

    private UUID userId;
    private String friendEmail;
    private UUID assignedSeatId;

    @Column(name = "stripe_checkout_url",length = 1024)
    private String stripeCheckoutUrl;

    private String stripePaymentIntentId;

    @Enumerated(EnumType.STRING)
    private SplitStatus status;
}
