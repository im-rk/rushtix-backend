package com.rushtix.core.feature.ticketcategory.repository;

import com.rushtix.core.domain.entities.TicketCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TicketCategoryRepository extends JpaRepository<TicketCategory, UUID> {

    List<TicketCategory> findAllByEventIdOrderByDisplayOrderAsc(UUID eventId);

    void deleteAllByEventId(UUID eventId);
}
