package com.dalrae.ticketing.venue.controller;

import com.dalrae.ticketing.venue.dto.VenueCreateRequest;
import com.dalrae.ticketing.venue.dto.VenueCreateResponse;
import com.dalrae.ticketing.venue.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/venues")
public class AdminVenueController {

    private final VenueService venueService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VenueCreateResponse create(@Valid @RequestBody VenueCreateRequest request) {
        return venueService.create(request);
    }
}
