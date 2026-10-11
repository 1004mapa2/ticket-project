package com.dalrae.ticketing.concert.repository;

import com.dalrae.ticketing.concert.domain.Concert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConcertRepository extends JpaRepository<Concert, UUID> {
}
