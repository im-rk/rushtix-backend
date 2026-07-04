package com.rushtix.core.feature.grouppay.service;

import com.rushtix.core.domain.entities.GroupBooking;
import com.rushtix.core.domain.entities.GroupPaymentItem;
import com.rushtix.core.domain.enums.GroupBookingStatus;
import com.rushtix.core.domain.enums.SplitStatus;
import com.rushtix.core.feature.grouppay.dto.ClaimSplitLinkRequest;
import com.rushtix.core.feature.grouppay.dto.ClaimSplitLinkResponse;
import com.rushtix.core.feature.grouppay.repository.GroupBookingRepository;
import com.rushtix.core.feature.grouppay.repository.GroupPaymentItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class GroupBookingClaimService {
    private final GroupBookingRepository groupBookingRepository;
    private final GroupPaymentItemRepository groupPaymentItemRepository;

    @Transactional
    public ClaimSplitLinkResponse claimUnpaidSlot(ClaimSplitLinkRequest request)
    {
        GroupBooking saga=groupBookingRepository.findById(request.groupBookingId())
                .orElseThrow(()->new RuntimeException("Group Booking not found"));

        if(saga.getStatus()!= GroupBookingStatus.PENDING_GROUP_PAYMENT)
        {
            throw new IllegalStateException("This group booking link is no longer active. It may have already been completed or cancelled.");
        }
        if(saga.getExpiresAt().isBefore(OffsetDateTime.now()))
        {
            throw new IllegalStateException("This group booking link has expired. The 10-minute payment window has closed. Please ask your friend to create a new group booking.");
        }
        boolean alreadyClaimed=saga.getPaymentItems().stream()
                .anyMatch(item->request.friendEmail().trim().equalsIgnoreCase(item.getFriendEmail()));

        if (alreadyClaimed) {
            GroupPaymentItem existingItem = saga.getPaymentItems().stream()
                    .filter(item -> request.friendEmail().trim().equalsIgnoreCase(item.getFriendEmail()))
                    .findFirst().get();
            return new ClaimSplitLinkResponse(existingItem.getStripeCheckoutUrl(), "Returning existing active payment session.");
        }

        // Atomic check: Find an unallocated item row slot
        GroupPaymentItem openSlot = saga.getPaymentItems().stream()
                .filter(item -> item.getStatus() == SplitStatus.PENDING && (item.getFriendEmail() == null || item.getFriendEmail().trim().isEmpty()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("All ticket slots for this group have already been claimed!"));

        openSlot.setFriendEmail(request.friendEmail().trim().toLowerCase());
        groupPaymentItemRepository.save(openSlot);

        return new ClaimSplitLinkResponse(openSlot.getStripeCheckoutUrl(), "Ticket slot secured. Redirecting to payment gateway...");
    }
}
