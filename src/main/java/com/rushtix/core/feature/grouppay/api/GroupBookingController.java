package com.rushtix.core.feature.grouppay.api;

import com.rushtix.core.domain.entities.GroupBooking;
import com.rushtix.core.domain.enums.SplitStatus;
import com.rushtix.core.feature.grouppay.dto.ClaimSplitLinkRequest;
import com.rushtix.core.feature.grouppay.dto.ClaimSplitLinkResponse;
import com.rushtix.core.feature.grouppay.dto.GroupBookingSagaStatusResponse;
import com.rushtix.core.feature.grouppay.dto.InitiateGroupRequest;
import com.rushtix.core.feature.grouppay.dto.InitiateGroupResponse;
import com.rushtix.core.feature.grouppay.repository.GroupBookingRepository;
import com.rushtix.core.feature.grouppay.service.GroupBookingClaimService;
import com.rushtix.core.feature.grouppay.service.GroupBookingInitializationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/group-booking")
@RequiredArgsConstructor
public class GroupBookingController {
    private final GroupBookingInitializationService initializationService;
    private final GroupBookingClaimService claimService;
    private final GroupBookingRepository groupBookingRepository;

    @PostMapping("/initiate")
    public ResponseEntity<InitiateGroupResponse> initializeSplitPay(@RequestBody @Valid InitiateGroupRequest request) {
        return ResponseEntity.ok(initializationService.convertToGroupPaySaga(request));
    }

    @PostMapping("/claim")
    public ResponseEntity<ClaimSplitLinkResponse> claimSlot(@RequestBody @Valid ClaimSplitLinkRequest request) {
        return ResponseEntity.ok(claimService.claimUnpaidSlot(request));
    }

    /**
     * Returns the live status of the group booking saga — used by the leader's dashboard.
     */
    @GetMapping("/status/{sagaId}")
    public ResponseEntity<GroupBookingSagaStatusResponse> getSagaStatus(@PathVariable UUID sagaId) {
        GroupBooking saga = groupBookingRepository.findById(sagaId)
                .orElseThrow(() -> new RuntimeException("Group booking not found"));

        long paidCount = saga.getPaymentItems().stream()
                .filter(item -> item.getStatus() == SplitStatus.PAID)
                .count();

        var items = saga.getPaymentItems().stream()
                .map(item -> new GroupBookingSagaStatusResponse.PaymentItemStatus(
                        item.getId(),
                        item.getFriendEmail(),
                        item.getStatus().toString(),
                        item.getStatus() != SplitStatus.PAID ? item.getStripeCheckoutUrl() : null
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new GroupBookingSagaStatusResponse(
                saga.getId(),
                saga.getStatus().toString(),
                saga.getPaymentItems().size(),
                (int) paidCount,
                items
        ));
    }

    /**
     * Returns the Stripe checkout URL for a specific email in this group booking.
     * Used by the leader's "Pay Your Share" button — no localStorage needed.
     */
    @GetMapping("/checkout-url/{sagaId}")
    public ResponseEntity<ClaimSplitLinkResponse> getCheckoutUrlForEmail(
            @PathVariable UUID sagaId,
            @RequestParam String email) {
        GroupBooking saga = groupBookingRepository.findById(sagaId)
                .orElseThrow(() -> new RuntimeException("Group booking not found"));

        var item = saga.getPaymentItems().stream()
                .filter(i -> email.trim().equalsIgnoreCase(i.getFriendEmail()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No payment slot found for this email in this group booking"));

        if (item.getStatus() == SplitStatus.PAID) {
            return ResponseEntity.ok(new ClaimSplitLinkResponse(null, "ALREADY_PAID"));
        }

        return ResponseEntity.ok(new ClaimSplitLinkResponse(item.getStripeCheckoutUrl(), "Redirecting to your payment session."));
    }

    /**
     * Look up the saga status by a payment item ID — used by the friend's success page
     * to show the group booking progress without needing the sagaId.
     */
    @GetMapping("/status-by-item/{itemId}")
    public ResponseEntity<GroupBookingSagaStatusResponse> getSagaStatusByItem(@PathVariable UUID itemId) {
        GroupBooking saga = groupBookingRepository.findByPaymentItemId(itemId)
                .orElseThrow(() -> new RuntimeException("Group booking not found for item"));

        long paidCount = saga.getPaymentItems().stream()
                .filter(i -> i.getStatus() == SplitStatus.PAID)
                .count();

        var items = saga.getPaymentItems().stream()
                .map(i -> new GroupBookingSagaStatusResponse.PaymentItemStatus(
                        i.getId(),
                        i.getFriendEmail(),
                        i.getStatus().toString(),
                        i.getStatus() != SplitStatus.PAID ? i.getStripeCheckoutUrl() : null
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new GroupBookingSagaStatusResponse(
                saga.getId(),
                saga.getStatus().toString(),
                saga.getPaymentItems().size(),
                (int) paidCount,
                items
        ));
    }
}
