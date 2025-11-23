package com.flightapp.repository;

import com.flightapp.entity.Flight;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

// This repo is for the flights collection.
// Earlier in MySQL I used @Query.but now we can use our own method names.

public interface FlightRepository extends ReactiveMongoRepository<Flight, String> {

	// This method makes MongoDB find matching flights inside a date range.
	Flux<Flight> findByFromPlaceAndToPlaceAndDepartureTimeBetween(String fromPlace, String toPlace, LocalDateTime start,
			LocalDateTime end);

	// In case I need to fetch flights by flightNumber
	Flux<Flight> findByFlightNumber(String flightNumber);
}
