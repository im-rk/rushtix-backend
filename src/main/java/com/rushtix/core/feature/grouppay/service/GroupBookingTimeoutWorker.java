package com.rushtix.core.feature.grouppay.service;

import com.rushtix.core.domain.entities.GroupBooking;
import com.rushtix.core.domain.enums.GroupBookingStatus;
import com.rushtix.core.feature.grouppay.repository.GroupBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GroupBookingTimeoutWorker {

    private final GroupBookingRepository groupBookingRepository;
    private final GroupPaySagaOrchestrator groupPaySagaOrchestrator;

    @Scheduled(fixedRate = 300000)
    public void scanAndEvictExpiredSagas()
    {
        List<GroupBooking> expiredGroups=groupBookingRepository
                .findAllByStatusAndExpiresAtBefore(GroupBookingStatus.PENDING_GROUP_PAYMENT, OffsetDateTime.now());

        for(GroupBooking failedSaga:expiredGroups)
        {
            groupPaySagaOrchestrator.rollbackSaga(failedSaga.getId());
        }

    }
}
