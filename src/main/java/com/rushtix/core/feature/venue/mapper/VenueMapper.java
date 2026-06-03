package com.rushtix.core.feature.venue.mapper;

import com.rushtix.core.domain.entities.Venue;
import com.rushtix.core.feature.venue.dto.OrganizerVenueResponse;
import com.rushtix.core.feature.venue.dto.VenueRequest;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING
        , unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VenueMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizer", ignore = true) // Set by service to link owner
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "seatMapConfig",ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Venue toEntity(VenueRequest request);

    OrganizerVenueResponse toOrganizerVenueResponse(Venue venue);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "seatMapConfig", ignore = true) // Protect the map from accidental wipe
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    void updateEntityFromRequest(VenueRequest request, @MappingTarget Venue venue);
}
