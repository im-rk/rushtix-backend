//package com.rushtix.core.feature.grouppay.service;
//
//import com.rushtix.core.domain.entities.GroupBooking;
//import com.rushtix.core.domain.entities.Seat;
//import com.rushtix.core.domain.enums.GroupBookingStatus;
//import com.rushtix.core.domain.enums.SeatStatus;
//import com.rushtix.core.domain.enums.SplitStatus;
//import com.rushtix.core.feature.booking.service.PaymentGatewayService;
//import com.rushtix.core.feature.booking.service.RedisLockService;
//import com.rushtix.core.feature.grouppay.repository.GroupBookingRepository;
//import com.rushtix.core.feature.seat.repository.SeatRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import javax.swing.*;
//import java.util.UUID;
//import java.util.concurrent.CompletableFuture;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class GroupPaySagaOrchestrator {
//    private final GroupBookingRepository groupBookingRepository;
//    private final SeatRepository seatRepository;
//    private final RedisLockService redisLockService;
//    private final PaymentGatewayService paymentGatewayService;
//
//    @Transactional
//    public void processWebhookPaymentSuccess(UUID paymentItemId,String stripePaymentIntentId) {
//        GroupBooking saga=groupBookingRepository.findByPaymentItemId(paymentItemId)
//                .orElseThrow(()->new RuntimeException("Group booking not found for payment item id: "+paymentItemId));
//
//        if (saga.getStatus() != GroupBookingStatus.PENDING_GROUP_PAYMENT) {
//            log.warn("Late payment received for already closed Saga session: {}", saga.getId());
//            return;
//        }
//
//        saga.getPaymentItems().stream()
//                .filter(item->item.getId().equals(paymentItemId))
//                .findFirst()
//                .ifPresent(item->{
//                    if(item.getStatus()!= SplitStatus.PAID)
//                    {
//                        item.setStatus(SplitStatus.PAID);
//                        item.setStripePaymentIntentId(stripePaymentIntentId);
//                    }
//                });
//
//        boolean totalGroupSuccess=saga.getPaymentItems().stream()
//                .allMatch(item->item.getStatus()==SplitStatus.PAID);
//        if(totalGroupSuccess)
//        {
//            commitSaga(saga);
//        }
//    }
//
//    @Transactional
//    public void commitSaga(GroupBooking saga)
//    {
//        log.info("saga Condition MET.Commiting group booking tickets:{}",saga.getId());
//        saga.setStatus(GroupBookingStatus.CONFIRMED);
//        groupBookingRepository.save(saga);
//
//        saga.getPaymentItems().forEach(item->{
//            Seat seat=seatRepository.findById(item.getAssignedSeatId()).orElseThrow();
//            seat.setStatus(SeatStatus.BOOKED);
//            seat.setQrToken("RUSH-GROUP-"+UUID.randomUUID().toString().replace("-","").toUpperCase());
//            seatRepository.save(seat);
//        });
//
//        saga.getPaymentItems().forEach(item->
//                redisLockService.releaseSeatLocks(java.util.List.of(item.getAssignedSeatId()),saga.getInitiatorUserId()));
//    }
//
//    @Transactional
//    public void rollbackSaga(UUID groupBookingId)
//    {
//        GroupBooking saga=groupBookingRepository.findById(groupBookingId).orElseThrow();
//        if(saga.getStatus()!=GroupBookingStatus.PENDING_GROUP_PAYMENT) return;
//        log.error("Compensating Transaction Triggered: Rolling back Group Booking {}", groupBookingId);
//
//        saga.setStatus(GroupBookingStatus.FAILED_REJECTED);
//        groupBookingRepository.save(saga);
//
//        saga.getPaymentItems().forEach(item->{
//            Seat seat=seatRepository.findById(item.getAssignedSeatId()).orElseThrow();
//            seat.setStatus(SeatStatus.AVAILABLE);
//            seat.setLockedBy(null);
//            seat.setStatus(null);
//            seatRepository.save(seat);
//
//            redisLockService.releaseSeatLocks(java.util.List.of(item.getAssignedSeatId()), saga.getInitiatorUserId());
//
//        });
//
//        saga.getPaymentItems().stream()
//                .filter(item -> item.getStatus() == SplitStatus.PAID)
//                .forEach(item -> CompletableFuture.runAsync(() -> {
//                    try {
//                        paymentGatewayService.refundCheckoutPayment(item.getStripePaymentIntentId());
//                        item.setStatus(SplitStatus.REFUNDED);
//                        log.info("Compensating partial refund executed for transaction entry item: {}", item.getId());
//                    } catch (Exception e) {
//                        log.error("CRITICAL ERROR: Automated compensation breakdown on Stripe intent {}", item.getStripePaymentIntentId(), e);
//                    }
//                }));
//    }
//
//}
