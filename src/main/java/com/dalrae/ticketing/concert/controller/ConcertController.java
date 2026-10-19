package com.dalrae.ticketing.concert.controller;

import com.dalrae.ticketing.concert.dto.ConcertSummaryResponse;
import com.dalrae.ticketing.concert.service.ConcertService;
import com.dalrae.ticketing.global.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ConcertController {

    private final ConcertService concertService;

    @GetMapping("/concerts")
    public PageResponse<ConcertSummaryResponse> getConcerts(@PageableDefault(size = 20) Pageable pageable) {
        return concertService.getUpcomingConcerts(pageable);
    }
}