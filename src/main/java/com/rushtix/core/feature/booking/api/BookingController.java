package com.rushtix.core.feature.booking.api;

import com.rushtix.core.domain.entities.User;
import com.rushtix.core.feature.booking.dto.BookingRequst;
import com.rushtix.core.feature.booking.dto.BookingReservationResponse;
import com.rushtix.core.feature.booking.dto.BookingUserResponse;
import com.rushtix.core.feature.booking.service.BookingUserService;
import com.rushtix.core.security.SecurityUtils;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingUserService bookingUserService;
    private final EntityManager entityManager;

    //Locks seats and returns the bare reservation container details
    @PostMapping("/reserve")
    public ResponseEntity<BookingReservationResponse> reserve(@RequestBody @Valid BookingRequst bookingRequst) {
        UUID userId = SecurityUtils.getCurrentUserId();
        User userContext = entityManager.getReference(User.class, userId);

        BookingReservationResponse response = bookingUserService.createBookingReservation(bookingRequst, userContext);
        return ResponseEntity.ok(response);
    }

    //Returns the full booking details ALONG WITH the Stripe client keys
    @PostMapping("/{bookingId}/pay-single")
    public ResponseEntity<BookingUserResponse> executeSinglePay(
            @PathVariable UUID bookingId,
            @RequestParam String idempotencyKey) {

        BookingUserResponse response = bookingUserService.initiateSinglePaymentExecution(bookingId, idempotencyKey);
        return ResponseEntity.ok(response);
    }

    //Returns booking details for the user checkout page
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingUserResponse> getBookingDetails(
            @PathVariable UUID bookingId) {
        
        UUID userId = SecurityUtils.getCurrentUserId();
        BookingUserResponse response = bookingUserService.getBookingDetailsForUser(bookingId, userId);
        return ResponseEntity.ok(response);
    }
//    @GetMapping("/{bookingId}/status")
//    public ResponseEntity<BookingStatusResponse> checkBookingFulfillmentStatus(
//            @PathVariable UUID bookingId,
//            @AuthenticationPrincipal User userContext) {
//
//        // 1. Fetch the latest state straight from PostgreSQL
//        BookingStatusResponse status = bookingUserService.getFulfillmentStatus(bookingId, userContext.getId());
//
//        return ResponseEntity.ok(status);
//    }
}
