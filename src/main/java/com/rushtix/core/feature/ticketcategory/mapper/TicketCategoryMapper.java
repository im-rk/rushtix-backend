package com.rushtix.core.feature.ticketcategory.mapper;

import com.rushtix.core.domain.entities.TicketCategory;
import com.rushtix.core.feature.ticketcategory.dto.TicketCategoryRequest;
import com.rushtix.core.feature.ticketcategory.dto.TicketCategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TicketCategoryMapper {

    /**
     * Convert Request DTO to Entity
     * CRITICAL: Ignore event (set by service), seatsSold, seatsLocked (always start at 0)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)          // Set by Service
    @Mapping(target = "currentPrice", ignore = true)   // Set by Service (initially = basePrice)
    @Mapping(target = "seatsSold", ignore = true)      // Always start at 0
    @Mapping(target = "seatsLocked", ignore = true)    // Always start at 0
    @Mapping(target = "version", ignore = true)        // Optimistic locking
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TicketCategory toEntity(TicketCategoryRequest request);

    /**
     * Convert Entity to Response DTO
     * Calculates availableSeats on the fly
     */
    @Mapping(source = "category.id", target = "id")
    @Mapping(source = "category.name", target = "name")
    @Mapping(source = "category.basePrice", target = "basePrice")
    @Mapping(source = "category.currentPrice", target = "currentPrice")
    @Mapping(source = "category.minPrice", target = "minPrice")
    @Mapping(source = "category.maxPrice", target = "maxPrice")
    @Mapping(source = "category.capacity", target = "capacity")
    @Mapping(source = "category.seatsSold", target = "seatsSold")
    @Mapping(source = "category.seatsLocked", target = "seatsLocked")
    @Mapping(expression = "java(category.getCapacity() - category.getSeatsSold() - category.getSeatsLocked())",
             target = "availableSeats")
    @Mapping(source = "category.displayOrder", target = "displayOrder")
    @Mapping(source = "category.pricingAlgorithm", target = "pricingAlgorithm")
    @Mapping(source = "category.dynamicPricingEnabled", target = "dynamicPricingEnabled")
    TicketCategoryResponse toResponse(TicketCategory category);

    /**
     * Update Entity from Request DTO
     * Protect: event (cannot change), seatsSold, seatsLocked (booking service controls)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)          // Cannot change event
    @Mapping(target = "seatsSold", ignore = true)      // Booking service controls
    @Mapping(target = "seatsLocked", ignore = true)    // Booking service controls
    @Mapping(target = "version", ignore = true)        // Optimistic locking
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(TicketCategoryRequest request, @MappingTarget TicketCategory category);
}
