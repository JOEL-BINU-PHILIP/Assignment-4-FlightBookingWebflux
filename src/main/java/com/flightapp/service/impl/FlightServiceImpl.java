package com.flightapp.service.impl;

import com.flightapp.dto.FlightInventoryRequest;
import com.flightapp.dto.FlightSearchRequest;
import com.flightapp.entity.Airline;
import com.flightapp.entity.Flight;
import com.flightapp.exception.ApiException;
import com.flightapp.repository.AirlineRepository;
import com.flightapp.repository.FlightRepository;
import com.flightapp.service.FlightService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

// This service handles all the "flight" logic.

@Service
public class FlightServiceImpl implements FlightService {

	private final FlightRepository flightRepository;
	private final AirlineRepository airlineRepository;

	public FlightServiceImpl(FlightRepository flightRepository, AirlineRepository airlineRepository) {
		this.flightRepository = flightRepository;
		this.airlineRepository = airlineRepository;
	}

	@Override
	public Mono<Flight> addInventory(FlightInventoryRequest request) {

		// I added some basic validations here.
		if (request.getDepartureTime().isAfter(request.getArrivalTime())) {
			return Mono.error(new ApiException("Departure time must be before arrival time"));
		}

		if (request.getTotalSeats() <= 0) {
			return Mono.error(new ApiException("Total seats must be > 0"));
		}

		if (request.getPrice() <= 0) {
			return Mono.error(new ApiException("Price must be > 0"));
		}

		// Find airline OR create a new one if not exists
		Mono<Airline> airlineMono = airlineRepository.findByName(request.getAirlineName())
				.switchIfEmpty(airlineRepository.save(
						Airline.builder().name(request.getAirlineName()).logoUrl(request.getAirlineLogoUrl()).build()));

		// After we get the airline, create a flight
		return airlineMono.flatMap(airline -> {

			Flight flight = Flight.builder().flightNumber(request.getFlightNumber()).fromPlace(request.getFromPlace())
					.toPlace(request.getToPlace()).departureTime(request.getDepartureTime())
					.arrivalTime(request.getArrivalTime()).price(request.getPrice()).totalSeats(request.getTotalSeats())
					.availableSeats(request.getTotalSeats()) // initially all seats available
					.airlineId(airline.getId()).build();

			// Mongo just stores the document, simple!
			return flightRepository.save(flight);
		});
	}

	@Override
	public Flux<Flight> searchFlights(FlightSearchRequest req) {

		// Converting the LocalDate into a start & end range.
		LocalDate date = req.getTravelDate();
		LocalDateTime start = date.atStartOfDay();
		LocalDateTime end = date.atTime(23, 59, 59);

		// This calls my method in FlightRepository.
		return flightRepository.findByFromPlaceAndToPlaceAndDepartureTimeBetween(req.getFromPlace(), req.getToPlace(),
				start, end);
	}

	@Override
	public Mono<Flight> getFlightById(String id) {
		return flightRepository.findById(id).switchIfEmpty(Mono.error(new ApiException("Flight not found: " + id)));
	}
}
