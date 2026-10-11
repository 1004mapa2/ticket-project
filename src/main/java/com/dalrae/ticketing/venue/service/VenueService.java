package com.dalrae.ticketing.venue.service;

import com.dalrae.ticketing.venue.domain.Venue;
import com.dalrae.ticketing.venue.dto.VenueCreateRequest;
import com.dalrae.ticketing.venue.dto.VenueCreateResponse;
import com.dalrae.ticketing.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VenueService {

    private final VenueRepository venueRepository;

    @Transactional
    public VenueCreateResponse create(VenueCreateRequest request) {
        Venue venue = venueRepository.save(request.toEntity());
        return new VenueCreateResponse(venue.getVenueId());
    }
}
