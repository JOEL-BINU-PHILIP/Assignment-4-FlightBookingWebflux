package com.flightapp.repository;

import com.flightapp.entity.Flight;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

// This repo is for the flights collection.

public interface FlightRepository extends ReactiveMongoRepository<Flight, String> {

	// MongoDB method to search flights inside a date range.
	Flux<Flight> findByFromPlaceAndToPlaceAndDepartureTimeBetween(String fromPlace, String toPlace, LocalDateTime start,
			LocalDateTime end);

	// Fetch flights by flightNumber if needed
	Flux<Flight> findByFlightNumber(String flightNumber);

	// New method for duplicate checking (flightNumber + departureTime combo)
	// Useful so we don't accidentally create the same flight twice.
	Mono<Flight> findByFlightNumberAndDepartureTime(String flightNumber, LocalDateTime departureTime);
}
