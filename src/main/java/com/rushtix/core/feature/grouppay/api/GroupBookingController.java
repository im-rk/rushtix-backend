package com.rushtix.core.feature.grouppay.api;

import com.rushtix.core.feature.grouppay.dto.ClaimSplitLinkRequest;
import com.rushtix.core.feature.grouppay.dto.ClaimSplitLinkResponse;
import com.rushtix.core.feature.grouppay.dto.InitiateGroupRequest;
import com.rushtix.core.feature.grouppay.dto.InitiateGroupResponse;
import com.rushtix.core.feature.grouppay.service.GroupBookingClaimService;
import com.rushtix.core.feature.grouppay.service.GroupBookingInitializationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/group-booking")
@RequiredArgsConstructor
public class GroupBookingController {
    private final GroupBookingInitializationService initializationService;
    private final GroupBookingClaimService claimService;

    @org.springframework.web.bind.annotation.PostMapping("/initiate")
    public ResponseEntity<InitiateGroupResponse> initializeSplitPay(@RequestBody @Valid InitiateGroupRequest request)
    {
        return ResponseEntity.ok(initializationService.convertToGroupPaySaga(request));
    }

    @org.springframework.web.bind.annotation.PostMapping("/claim")
    public ResponseEntity<ClaimSplitLinkResponse> claimSlot(@RequestBody @Valid ClaimSplitLinkRequest request)
    {
        return ResponseEntity.ok(claimService.claimUnpaidSlot(request));
    }
    @org.springframework.web.bind.annotation.GetMapping("/debug/{sagaId}")
    public ResponseEntity<?> debugGroupBooking(@org.springframework.web.bind.annotation.PathVariable java.util.UUID sagaId, @org.springframework.beans.factory.annotation.Autowired com.rushtix.core.feature.grouppay.repository.GroupBookingRepository groupBookingRepository) {
        var saga = groupBookingRepository.findById(sagaId).orElse(null);
        if (saga == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(saga.getPaymentItems().stream().map(item -> 
            java.util.Map.of(
                "id", item.getId(),
                "friendEmail", item.getFriendEmail() == null ? "NULL" : item.getFriendEmail(),
                "status", item.getStatus()
            )
        ).toList());
    }
}
