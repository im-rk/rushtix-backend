package com.rushtix.core.feature.ticketcategory.repository;

import com.rushtix.core.domain.entities.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TicketCategoryRepository extends JpaRepository<TicketCategory, UUID> {

    /**
     * Get all categories for an event, ordered by display order (ASC)
     * CRITICAL: This ensures VIP shows before Gold shows before General
     * Uses composite index: (event_id, display_order)
     */
    List<TicketCategory> findAllByEventIdOrderByDisplayOrderAsc(UUID eventId);
}
