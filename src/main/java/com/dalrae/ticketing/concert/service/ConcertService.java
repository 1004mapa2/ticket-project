package com.dalrae.ticketing.concert.service;

import com.dalrae.ticketing.concert.domain.Concert;
import com.dalrae.ticketing.concert.dto.ConcertCreateRequest;
import com.dalrae.ticketing.concert.dto.ConcertCreateResponse;
import com.dalrae.ticketing.concert.repository.ConcertRepository;
import com.dalrae.ticketing.global.exception.BusinessException;
import com.dalrae.ticketing.global.exception.ErrorCode;
import com.dalrae.ticketing.venue.domain.Venue;
import com.dalrae.ticketing.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final VenueRepository venueRepository;


    @Transactional
    public ConcertCreateResponse createConcert(ConcertCreateRequest request) {
        Venue venue = venueRepository.findById(request.venueId()).orElseThrow(() -> new BusinessException(ErrorCode.VENUE_NOT_FOUND));
        Concert concert = concertRepository.save(request.toEntity(venue));
        return new ConcertCreateResponse(concert.getConcertId());
    }
}
