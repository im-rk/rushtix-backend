package com.rushtix.core.feature.booking.scheduler;

import com.rushtix.core.domain.entities.Booking;
import com.rushtix.core.domain.entities.Seat;
import com.rushtix.core.domain.enums.BookingStatus;
import com.rushtix.core.domain.enums.SeatStatus;
import com.rushtix.core.feature.booking.repository.BookingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingCleanUpScheduler {
    private final BookingRepository bookingRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanUpExpiredBookings() {
        OffsetDateTime now = OffsetDateTime.now();

        List<Booking> expiredBookings = bookingRepository.findAllByStatusAndExpiresAtBefore(BookingStatus.PENDING, now);

        if (expiredBookings.isEmpty()) {
            return;
        }
        log.info("Found {} expired ticket reservations. Initiating auto-release protocol...", expiredBookings.size());
        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancellationReason("Abonded reservation - auto cancelled by system");
            for (Seat seat : booking.getSeats()) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setBooking(null);
                seat.setLockedBy(null);
                seat.setLockedUntil(null);
            }
        }
        bookingRepository.saveAll(expiredBookings);
        log.info("Successfully synchronized database state. All leaked seat inventory is back on the market.");
    }
}
