package com.rushtix.core.feature.grouppay.repository;

import com.rushtix.core.domain.entities.GroupBooking;
import com.rushtix.core.domain.enums.GroupBookingStatus;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupBookingRepository extends JpaRepository<GroupBooking, UUID> {
    List<GroupBooking> findAllByStatusAndExpiresAtBefore(GroupBookingStatus status, OffsetDateTime dateTime);
    @org.springframework.data.jpa.repository.Query("SELECT gb FROM GroupBooking gb JOIN gb.paymentItems pi WHERE pi.id = :paymentItemId")
    Optional<GroupBooking> findByPaymentItemId(@Param("paymentItemId") UUID paymentItemId);

}

