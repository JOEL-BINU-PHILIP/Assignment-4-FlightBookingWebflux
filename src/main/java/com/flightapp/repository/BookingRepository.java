package com.flightapp.repository;

import com.flightapp.entity.Booking;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

// This repo manages all Booking documents.
public interface BookingRepository extends ReactiveMongoRepository<Booking, String> {

	// Find booking by its PNR. Used for ticket lookup.
	Mono<Booking> findByPnr(String pnr);

	// List all bookings done by a particular user (emailId)
	Flux<Booking> findByEmail(String email);
}
