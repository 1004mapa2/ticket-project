package com.dalrae.ticketing.concert.service;

import com.dalrae.ticketing.concert.domain.Concert;
import com.dalrae.ticketing.concert.domain.ConcertSchedule;
import com.dalrae.ticketing.concert.dto.ConcertCreateRequest;
import com.dalrae.ticketing.concert.dto.ConcertCreateResponse;
import com.dalrae.ticketing.concert.dto.ScheduleCreateRequest;
import com.dalrae.ticketing.concert.dto.ScheduleCreateResponse;
import com.dalrae.ticketing.concert.repository.ConcertRepository;
import com.dalrae.ticketing.concert.repository.ConcertScheduleRepository;
import com.dalrae.ticketing.global.exception.BusinessException;
import com.dalrae.ticketing.global.exception.ErrorCode;
import com.dalrae.ticketing.venue.domain.Venue;
import com.dalrae.ticketing.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final VenueRepository venueRepository;
    private final ConcertScheduleRepository concertScheduleRepository;
    private final Clock clock;


    @Transactional
    public ConcertCreateResponse createConcert(ConcertCreateRequest request) {
        Venue venue = venueRepository.findById(request.venueId()).orElseThrow(() -> new BusinessException(ErrorCode.VENUE_NOT_FOUND));
        Concert concert = concertRepository.save(request.toEntity(venue));
        return new ConcertCreateResponse(concert.getConcertId());
    }

    public ScheduleCreateResponse createSchedule(UUID concertId, ScheduleCreateRequest request) {
        if (!request.performanceAt().isAfter(OffsetDateTime.now(clock))) {
            throw new BusinessException(ErrorCode.PAST_PERFORMANCE_TIME);
        }
        Concert concert = concertRepository.findById(concertId).orElseThrow(() -> new BusinessException(ErrorCode.CONCERT_NOT_FOUND));
        if (concertScheduleRepository.existsByConcertAndPerformanceAt(concert, request.performanceAt())) {
            throw new BusinessException(ErrorCode.DUPLICATE_SCHEDULE);
        }
        ConcertSchedule concertSchedule = concertScheduleRepository.save(request.toEntity(concert));
        return new ScheduleCreateResponse(concertSchedule.getScheduleId());

    }
}
