package com.dalrae.ticketing.concert.service;

import com.dalrae.ticketing.concert.domain.ConcertSchedule;
import com.dalrae.ticketing.concert.domain.Seat;
import com.dalrae.ticketing.concert.dto.SeatCreateRequest;
import com.dalrae.ticketing.concert.dto.SeatCreateResponse;
import com.dalrae.ticketing.concert.repository.ConcertScheduleRepository;
import com.dalrae.ticketing.concert.repository.SeatRepository;
import com.dalrae.ticketing.global.exception.BusinessException;
import com.dalrae.ticketing.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {

    private static final int MAX_SEAT_PER_REQUEST = 5_000;

    private final ConcertScheduleRepository concertScheduleRepository;
    private final SeatRepository seatRepository;
    private final Clock clock;

    @Transactional
    public SeatCreateResponse createSeats(UUID scheduleId, SeatCreateRequest request) {
        validateRange(request);

        ConcertSchedule schedule = concertScheduleRepository.findById(scheduleId).orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (!schedule.isBeforeBookingOpen(OffsetDateTime.now(clock))) {
            throw new BusinessException(ErrorCode.SEAT_CREATION_CLOSED);
        }

        if (seatRepository.existsByScheduleAndSectionAndSeatRowBetweenAndSeatNumberBetween(schedule, request.section(), request.rowFrom(), request.rowTo(), request.numberFrom(), request.numberTo())) {
            throw new BusinessException(ErrorCode.DUPLICATE_SEAT);
        }

        List<Seat> seats = request.toEntities(schedule);
        seatRepository.saveAll(seats);
        return new SeatCreateResponse(seats.size());
    }

    private static void validateRange(SeatCreateRequest request) {
        if (request.rowFrom() > request.rowTo() || request.numberFrom() > request.numberTo()) {
            throw new BusinessException(ErrorCode.INVALID_SEAT_RANGE);
        }
        if (request.totalCount() > MAX_SEAT_PER_REQUEST) {
            throw new BusinessException(ErrorCode.INVALID_SEAT_RANGE);
        }
    }
}
