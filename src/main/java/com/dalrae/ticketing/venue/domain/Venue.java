package com.dalrae.ticketing.venue.domain;

import com.dalrae.ticketing.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Entity
@Table(name = "venues")
@Getter
public class Venue extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID venueId;

    private String name;

    private String address;

    protected Venue() {
    }

    @Builder
    private Venue(String name, String address) {
        this.name = name;
        this.address = address;
    }
}
