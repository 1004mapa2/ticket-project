package com.dalrae.ticketing.concert.controller;

import com.dalrae.ticketing.concert.dto.*;
import com.dalrae.ticketing.concert.service.ConcertService;
import com.dalrae.ticketing.concert.service.SeatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminConcertController {

    private final ConcertService concertService;
    private final SeatService seatService;

    @PostMapping("/concerts")
    @ResponseStatus(HttpStatus.CREATED)
    public ConcertCreateResponse createConcert(@Valid @RequestBody ConcertCreateRequest request) {
        return concertService.createConcert(request);
    }

    @PostMapping("/concerts/{concertId}/schedules")
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduleCreateResponse createSchedule(@PathVariable UUID concertId,
                                                 @Valid @RequestBody ScheduleCreateRequest request) {
        return concertService.createSchedule(concertId, request);
    }

    @PostMapping("/schedules/{scheduleId}/seats")
    @ResponseStatus(HttpStatus.CREATED)
    public SeatCreateResponse createSeats(@PathVariable UUID scheduleId,
                                          @Valid @RequestBody SeatCreateRequest request) {
        return seatService.createSeats(scheduleId, request);
    }
}
