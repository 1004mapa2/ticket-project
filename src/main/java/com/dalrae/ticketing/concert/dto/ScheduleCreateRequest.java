package com.dalrae.ticketing.concert.dto;

import com.dalrae.ticketing.concert.domain.Concert;
import com.dalrae.ticketing.concert.domain.ConcertSchedule;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record ScheduleCreateRequest(

        @NotNull
        OffsetDateTime performanceAt,

        @NotNull
        OffsetDateTime bookingOpenAt
) {
    public ConcertSchedule toEntity(Concert concert) {
        return ConcertSchedule.builder()
                .concert(concert)
                .performanceAt(performanceAt)
                .bookingOpenAt(bookingOpenAt)
                .build();
    }
}
