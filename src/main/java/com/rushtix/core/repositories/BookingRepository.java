package com.rushtix.core.repositories;

import com.rushtix.core.domain.entities.Booking;
import jakarta.validation.ReportAsSingleViolation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

}
