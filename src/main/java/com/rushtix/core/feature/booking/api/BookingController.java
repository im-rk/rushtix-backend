package com.rushtix.core.feature.booking.api;

import com.rushtix.core.domain.entities.User;
import com.rushtix.core.feature.booking.dto.BookingRequst;
import com.rushtix.core.feature.booking.dto.BookingUserResponse;
import com.rushtix.core.feature.booking.service.BookingUserService;
import com.rushtix.core.security.SecurityUtils;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingUserService bookingUserService;
    private final EntityManager entityManager;

    @PostMapping("/reserve")
    public BookingUserResponse confirmPayment(@RequestBody @Valid BookingRequst bookingRequst)
    {
        UUID userId= SecurityUtils.getCurrentUserId();
        User UserContext=entityManager.getReference(User.class,userId);
        return bookingUserService.createBookingReservation(bookingRequst,UserContext);
    }

    @PostMapping("/{bookingId}/confirm")
    public BookingUserResponse confirmBooking(@PathVariable UUID bookingId,@RequestParam("paymentToken") String paymentToken)
    {
        return bookingUserService.confirmBookingPayment(bookingId,paymentToken);
    }
}
