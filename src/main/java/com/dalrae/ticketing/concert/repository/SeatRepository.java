package com.dalrae.ticketing.concert.repository;

import com.dalrae.ticketing.concert.domain.ConcertSchedule;
import com.dalrae.ticketing.concert.domain.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, UUID> {

    boolean existsByScheduleAndSectionAndSeatRowBetweenAndSeatNumberBetween(ConcertSchedule schedule, String section, int rowFrom, int rowTo, int numberFrom, int numberTo);
}
