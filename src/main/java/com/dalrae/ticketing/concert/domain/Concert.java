package com.dalrae.ticketing.concert.domain;

import com.dalrae.ticketing.global.common.BaseTimeEntity;
import com.dalrae.ticketing.venue.domain.Venue;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "concerts")
@Getter
public class Concert extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID concertId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    private String title;

    private String artist;

    private String genre;

    protected Concert() {
    }

    @Builder
    private Concert(Venue venue, String title, String artist, String genre) {
        this.venue = venue;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
    }
}
