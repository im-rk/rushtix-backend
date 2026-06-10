package com.rushtix.core.feature.ticketcategory.service;

import com.rushtix.core.domain.entities.TicketCategory;
import com.rushtix.core.feature.ticketcategory.dto.PublicTicketCategoryResponse;
import com.rushtix.core.feature.ticketcategory.mapper.TicketCategoryMapper;
import com.rushtix.core.feature.ticketcategory.repository.TicketCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketCategoryUserService {

    private final TicketCategoryRepository ticketCategoryRepository;
    private final TicketCategoryMapper ticketCategoryMapper;

    @Transactional(readOnly = true)
    public List<PublicTicketCategoryResponse> getPublicCategoriesForEvent(UUID eventId) {
        List<TicketCategory> categories = ticketCategoryRepository.findAllByEventIdOrderByDisplayOrderAsc(eventId);

        if(categories.isEmpty())
        {
            throw new RuntimeException("No ticket categories found for event with ID: " + eventId);
        }
        return ticketCategoryMapper.toPublicResponseList(categories);
    }

    @Transactional(readOnly = true)
    public PublicTicketCategoryResponse getPublicCategoryById(UUID categoryId) {
        TicketCategory category = ticketCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Ticket category not found with ID: " + categoryId));

        return ticketCategoryMapper.toPublicResponse(category);
    }
}
