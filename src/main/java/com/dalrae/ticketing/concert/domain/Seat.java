package com.dalrae.ticketing.concert.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "seats")
@Getter
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID seatId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ConcertSchedule schedule;

    private String section;

    private int seatRow;

    private int seatNumber;

    @Enumerated(EnumType.STRING)
    private SeatGrade grade;

    private int price;

    @Enumerated(EnumType.STRING)
    private SeatStatus status;

    protected Seat() {
    }

    @Builder
    private Seat(ConcertSchedule schedule, String section, int seatRow, int seatNumber, SeatGrade grade, int price) {
        this.schedule = schedule;
        this.section = section;
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;
        this.grade = grade;
        this.price = price;
        this.status = SeatStatus.AVAILABLE;
    }
}
