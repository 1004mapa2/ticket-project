package com.dalrae.ticketing.venue.repository;

import com.dalrae.ticketing.venue.domain.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VenueRepository extends JpaRepository<Venue, UUID> {
}
