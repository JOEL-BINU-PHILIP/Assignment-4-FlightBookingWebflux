package com.flightapp.service;

// Basically similar to previous Assignment
import com.flightapp.dto.FlightInventoryRequest;
import com.flightapp.dto.FlightSearchRequest;
import com.flightapp.entity.Flight;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// This interface defines what my Flight service should do.
// Basically all the flight-related business logic.

public interface FlightService {

	// Add new flight inventory (admin functionality)
	Mono<Flight> addInventory(FlightInventoryRequest request);

	// Search flights based on from/to/date
	Flux<Flight> searchFlights(FlightSearchRequest req);

	// Get a specific flight (useful for booking)
	Mono<Flight> getFlightById(String id);
}
