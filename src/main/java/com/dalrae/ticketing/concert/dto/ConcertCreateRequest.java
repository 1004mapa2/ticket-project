package com.dalrae.ticketing.concert.dto;

import com.dalrae.ticketing.concert.domain.Concert;
import com.dalrae.ticketing.venue.domain.Venue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ConcertCreateRequest(

        @NotNull
        UUID venueId,

        @NotBlank
        @Size(max = 255)
        String title,

        @Size(max = 255)
        String artist,

        @Size(max = 50)
        String genre
) {
    public Concert toEntity(Venue venue) {
        return Concert.builder()
                .venue(venue)
                .title(title)
                .artist(artist)
                .genre(genre)
                .build();
    }

}
