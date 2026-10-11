package com.dalrae.ticketing.concert.controller;

import com.dalrae.ticketing.concert.dto.ConcertCreateRequest;
import com.dalrae.ticketing.concert.dto.ConcertCreateResponse;
import com.dalrae.ticketing.concert.service.ConcertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminConcertController {

    private final ConcertService concertService;

    @PostMapping("/concerts")
    @ResponseStatus(HttpStatus.CREATED)
    public ConcertCreateResponse createConcert(@Valid @RequestBody ConcertCreateRequest request) {
        return concertService.createConcert(request);
    }
}
