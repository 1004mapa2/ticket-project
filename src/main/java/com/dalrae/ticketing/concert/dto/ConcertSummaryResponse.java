package com.dalrae.ticketing.concert.dto;

import com.dalrae.ticketing.concert.domain.Concert;

import java.util.UUID;

public record ConcertSummaryResponse(
        UUID concertId,
        String title,
        String artist,
        String genre,
        String venueName
) {
    public static ConcertSummaryResponse from(Concert concert) {
        return new ConcertSummaryResponse(
                concert.getConcertId(),
                concert.getTitle(),
                concert.getArtist(),
                concert.getGenre(),
                concert.getVenue().getName());
    }
}
