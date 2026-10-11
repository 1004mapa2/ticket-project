package com.dalrae.ticketing.concert.domain;

import com.dalrae.ticketing.global.common.BaseTimeEntity;
import com.dalrae.ticketing.global.exception.BusinessException;
import com.dalrae.ticketing.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "concert_schedules")
@Getter
public class ConcertSchedule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID scheduleId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concert_id", nullable = false)
    private Concert concert;

    private OffsetDateTime performanceAt;

    private OffsetDateTime bookingOpenAt;

    protected ConcertSchedule() {
    }

    @Builder
    private ConcertSchedule(Concert concert, OffsetDateTime performanceAt, OffsetDateTime bookingOpenAt) {
        if (!bookingOpenAt.isBefore(performanceAt)) {
            throw new BusinessException(ErrorCode.INVALID_SCHEDULE_TIME);
        }
        this.concert = concert;
        this.performanceAt = performanceAt;
        this.bookingOpenAt = bookingOpenAt;
    }
}
