package com.dalrae.ticketing.concert.repository;

import com.dalrae.ticketing.concert.domain.Concert;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface ConcertRepository extends JpaRepository<Concert, UUID> {

    @Query("""
            select c
            from Concert c
            where exists (
                select s
                from ConcertSchedule s
                where s.concert = c
                  and s.performanceAt > :now
            )
            """)
    Page<Concert> findUpcoming(@Param("now") OffsetDateTime now, Pageable pageable);
}
