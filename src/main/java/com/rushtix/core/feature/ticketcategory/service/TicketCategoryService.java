package com.rushtix.core.feature.ticketcategory.service;

import com.rushtix.core.domain.entities.Event;
import com.rushtix.core.domain.entities.TicketCategory;
import com.rushtix.core.feature.events.repository.EventRepository;
import com.rushtix.core.feature.ticketcategory.dto.TicketCategoryRequest;
import com.rushtix.core.feature.ticketcategory.dto.TicketCategoryResponse;
import com.rushtix.core.feature.ticketcategory.mapper.TicketCategoryMapper;
import com.rushtix.core.feature.ticketcategory.repository.TicketCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketCategoryService {

    private final TicketCategoryRepository ticketCategoryRepository;
    private final EventRepository eventRepository;
    private final TicketCategoryMapper mapper;

    /**
     * Create a new ticket category for an event
     *
     * Business Rules:
     * 1. Event must exist
     * 2. Total capacity across all categories cannot exceed event's totalSeats
     * 3. Price boundaries: minPrice <= basePrice <= maxPrice
     * 4. Initial state: currentPrice = basePrice, seatsSold = 0, seatsLocked = 0
     */
    @Transactional
    public TicketCategoryResponse createCategory(UUID eventId, TicketCategoryRequest request) {
        // Validate event exists
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Validate event has valid capacity
        if (event.getTotalSeats() <= 0) {
            throw new RuntimeException(
                    "Event has invalid total seats: " + event.getTotalSeats()
            );
        }

        // Validate price boundaries: minPrice <= basePrice <= maxPrice
        validatePriceBoundaries(request);

        // Check if display order already exists (must be unique per event)
        boolean displayOrderExists = ticketCategoryRepository.findAllByEventIdOrderByDisplayOrderAsc(eventId)
                .stream()
                .anyMatch(c -> c.getDisplayOrder() == request.displayOrder());

        if (displayOrderExists) {
            throw new RuntimeException(
                    "Display order " + request.displayOrder() + " already exists for this event. " +
                    "Each category must have a unique display order."
            );
        }

        // Business Rule: Check if total capacity of all categories + new category <= event's totalSeats
        int currentTotalCapacity = ticketCategoryRepository.findAllByEventIdOrderByDisplayOrderAsc(eventId)
                .stream()
                .mapToInt(TicketCategory::getCapacity)
                .sum();

        if (currentTotalCapacity + request.capacity() > event.getTotalSeats()) {
            throw new RuntimeException(
                    "Total category capacity (" + (currentTotalCapacity + request.capacity()) +
                            ") exceeds event limit (" + event.getTotalSeats() + ")"
            );
        }

        // Map request to entity
        TicketCategory category = mapper.toEntity(request);

        // Set relationships and defaults
        category.setEvent(event);
        category.setCurrentPrice(request.basePrice());  // Start at base price
        category.setSeatsSold(0);                        // No sales yet
        category.setSeatsLocked(0);                      // No locks yet

        // Save and return
        TicketCategory savedCategory = ticketCategoryRepository.save(category);
        return mapper.toResponse(savedCategory);
    }

    /**
     * Get all categories for an event (ordered by displayOrder for UI)
     *
     * @param eventId Event ID
     * @return List of categories in display order
     */
    @Transactional(readOnly = true)
    public List<TicketCategoryResponse> getCategoriesForEvent(UUID eventId) {
        return ticketCategoryRepository.findAllByEventIdOrderByDisplayOrderAsc(eventId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Get a single category by ID
     *
     * @param id Category ID
     * @return Category details
     */
    @Transactional(readOnly = true)
    public TicketCategoryResponse getCategoryById(UUID id) {
        TicketCategory category = ticketCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket category not found"));
        return mapper.toResponse(category);
    }

    /**
     * Update a ticket category
     *
     * Constraints:
     * - Cannot change event (use delete + create instead)
     * - Cannot change capacity if seats are sold
     * - Cannot change seatsSold/seatsLocked (booking service controls these)
     * - Price boundaries must be maintained
     */
    @Transactional
    public TicketCategoryResponse updateCategory(UUID id, TicketCategoryRequest request) {
        TicketCategory category = ticketCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket category not found"));

        Event event = eventRepository.findById(category.getEvent().getId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Validate price boundaries
        validatePriceBoundaries(request);

        // Check if capacity is being changed
        if (category.getCapacity() != request.capacity()) {
            // Prevent capacity change if seats are already sold
            if (category.getSeatsSold() > 0) {
                throw new RuntimeException(
                        "Cannot change capacity when seats have been sold. Current: " +
                        category.getSeatsSold() + " sold"
                );
            }

            // Validate that new total capacity doesn't exceed event limit
            int currentTotalCapacity = ticketCategoryRepository.findAllByEventIdOrderByDisplayOrderAsc(event.getId())
                    .stream()
                    .filter(c -> !c.getId().equals(id))  // Exclude current category
                    .mapToInt(TicketCategory::getCapacity)
                    .sum();

            if (currentTotalCapacity + request.capacity() > event.getTotalSeats()) {
                throw new RuntimeException(
                        "Total capacity (" + (currentTotalCapacity + request.capacity()) +
                        ") would exceed event limit (" + event.getTotalSeats() + ")"
                );
            }
        }

        // Check if display order is being changed
        if (category.getDisplayOrder() != request.displayOrder()) {
            // Check if new display order already exists for another category in this event
            boolean displayOrderExists = ticketCategoryRepository.findAllByEventIdOrderByDisplayOrderAsc(event.getId())
                    .stream()
                    .filter(c -> !c.getId().equals(id))  // Exclude current category
                    .anyMatch(c -> c.getDisplayOrder() == request.displayOrder());

            if (displayOrderExists) {
                throw new RuntimeException(
                        "Display order " + request.displayOrder() + " already exists for another category in this event"
                );
            }
        }

        // Map request to entity (ignores protected fields)
        mapper.updateEntityFromRequest(request, category);

        // Update currentPrice to basePrice when updating
        category.setCurrentPrice(request.basePrice());

        TicketCategory updatedCategory = ticketCategoryRepository.save(category);
        return mapper.toResponse(updatedCategory);
    }

    /**
     * Delete a ticket category
     *
     * Constraint: Only allow deletion if no seats have been sold
     */
    @Transactional
    public void deleteCategory(UUID id) {
        TicketCategory category = ticketCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket category not found"));

        if (category.getSeatsSold() > 0) {
            throw new RuntimeException(
                    "Cannot delete category with sold seats. Current sales: " + category.getSeatsSold()
            );
        }

        ticketCategoryRepository.delete(category);
    }

    /**
     * Update the current price (for dynamic pricing)
     *
     * Constraint: currentPrice must be within [minPrice, maxPrice]
     */
    @Transactional
    public TicketCategoryResponse updateCurrentPrice(UUID id, java.math.BigDecimal newPrice) {
        TicketCategory category = ticketCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket category not found"));

        // Validate price within boundaries
        if (newPrice.compareTo(category.getMinPrice()) < 0) {
            throw new RuntimeException(
                    "New price (" + newPrice + ") is below minimum (" + category.getMinPrice() + ")"
            );
        }

        if (newPrice.compareTo(category.getMaxPrice()) > 0) {
            throw new RuntimeException(
                    "New price (" + newPrice + ") is above maximum (" + category.getMaxPrice() + ")"
            );
        }

        category.setCurrentPrice(newPrice);
        TicketCategory updatedCategory = ticketCategoryRepository.save(category);
        return mapper.toResponse(updatedCategory);
    }

    // --- VALIDATION HELPERS ---

    /**
     * Validate that price boundaries are correct: minPrice <= basePrice <= maxPrice
     */
    private void validatePriceBoundaries(TicketCategoryRequest request) {
        if (request.minPrice().compareTo(request.basePrice()) > 0) {
            throw new RuntimeException(
                    "Minimum price (" + request.minPrice() +
                    ") cannot be greater than base price (" + request.basePrice() + ")"
            );
        }

        if (request.basePrice().compareTo(request.maxPrice()) > 0) {
            throw new RuntimeException(
                    "Base price (" + request.basePrice() +
                    ") cannot be greater than maximum price (" + request.maxPrice() + ")"
            );
        }

        if (request.minPrice().compareTo(request.maxPrice()) > 0) {
            throw new RuntimeException(
                    "Minimum price (" + request.minPrice() +
                    ") cannot be greater than maximum price (" + request.maxPrice() + ")"
            );
        }
    }
}
