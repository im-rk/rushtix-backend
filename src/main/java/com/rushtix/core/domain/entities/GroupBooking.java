package com.rushtix.core.domain.entities;

import com.rushtix.core.domain.enums.GroupBookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="group_bookings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID eventId;
    private UUID initiatorUserId;


    private GroupBookingStatus status;

    private BigDecimal totalAmount;
    private BigDecimal perPersonAmount;

    private OffsetDateTime expiresAt;
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy = "groupBooking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GroupPaymentItem> paymentItems;
}
