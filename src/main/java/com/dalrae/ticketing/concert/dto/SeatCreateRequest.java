package com.dalrae.ticketing.concert.dto;

import com.dalrae.ticketing.concert.domain.ConcertSchedule;
import com.dalrae.ticketing.concert.domain.Seat;
import com.dalrae.ticketing.concert.domain.SeatGrade;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public record SeatCreateRequest(

        @NotBlank
        @Size(max = 10)
        String section,

        @NotNull
        @Min(1)
        Integer rowFrom,

        @NotNull
        @Min(1)
        Integer rowTo,

        @NotNull
        @Min(1)
        Integer numberFrom,

        @NotNull
        @Min(1)
        Integer numberTo,

        @NotNull
        SeatGrade grade,

        @NotNull
        @Min(0)
        Integer price
) {
    public long totalCount() {
        return (long) (rowTo - rowFrom + 1) * (numberTo - numberFrom + 1);
    }

    public List<Seat> toEntities(ConcertSchedule schedule) {
        List<Seat> seats = new ArrayList<>((int) totalCount());
        for (int row = rowFrom; row <= rowTo; row++) {
            for (int number = numberFrom; number <= numberTo; number++) {
                seats.add(Seat.builder()
                        .schedule(schedule)
                        .section(section)
                        .seatRow(row)
                        .seatNumber(number)
                        .grade(grade)
                        .price(price)
                        .build());
            }
        }
        return seats;
    }
}
