package com.flightapp.repository;

import com.flightapp.entity.Passenger;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

// Repo for passenger documents.
// Each passenger belongs to a booking, so I fetch passengers using bookingId.

public interface PassengerRepository extends ReactiveMongoRepository<Passenger, String> {

	// Finds all passengers belonging to one booking
	Flux<Passenger> findByBookingId(String bookingId);
}
