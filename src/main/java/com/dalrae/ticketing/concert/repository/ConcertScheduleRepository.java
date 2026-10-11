package com.dalrae.ticketing.concert.repository;

import com.dalrae.ticketing.concert.domain.Concert;
import com.dalrae.ticketing.concert.domain.ConcertSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface ConcertScheduleRepository extends JpaRepository<ConcertSchedule, UUID> {

    boolean existsByConcertAndPerformanceAt(Concert concert, OffsetDateTime performanceAt);
}
