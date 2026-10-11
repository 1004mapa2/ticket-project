package com.dalrae.ticketing.venue.dto;

import com.dalrae.ticketing.venue.domain.Venue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VenueCreateRequest(
        @NotBlank
        @Size(max = 100)
        String name,

        @Size(max = 255)
        String address
) {
    public Venue toEntity() {
        return Venue.builder()
                .name(name)
                .address(address)
                .build();
    }
}
