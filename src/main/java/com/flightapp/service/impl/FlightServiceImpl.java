package com.flightapp.service.impl;

import com.flightapp.dto.FlightInventoryRequest;
import com.flightapp.dto.FlightSearchRequest;
import com.flightapp.entity.Airline;
import com.flightapp.entity.Flight;
import com.flightapp.exception.ApiException;
import com.flightapp.repository.AirlineRepository;
import com.flightapp.repository.FlightRepository;
import com.flightapp.service.FlightService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

// This service handles all the "flight" logic.

@Service
@Slf4j
public class FlightServiceImpl implements FlightService {

	private final FlightRepository flightRepository;
	private final AirlineRepository airlineRepository;

	public FlightServiceImpl(FlightRepository flightRepository, AirlineRepository airlineRepository) {
		this.flightRepository = flightRepository;
		this.airlineRepository = airlineRepository;
	}

	@Override
	public Mono<Flight> addInventory(FlightInventoryRequest request) {

		log.info("Received request to add new flight inventory");
		log.debug("FlightInventoryRequest payload: {}", request);

		// I added some basic validations here.
		if (request.getDepartureTime().isAfter(request.getArrivalTime())) {
			log.warn("Invalid time range: departure {} is after arrival {}", request.getDepartureTime(),
					request.getArrivalTime());
			return Mono.error(new ApiException("Departure time must be before arrival time"));
		}

		if (request.getTotalSeats() <= 0) {
			log.warn("Invalid totalSeats: {}", request.getTotalSeats());
			return Mono.error(new ApiException("Total seats must be > 0"));
		}

		if (request.getPrice() <= 0) {
			log.warn("Invalid price: {}", request.getPrice());
			return Mono.error(new ApiException("Price must be > 0"));
		}

		// Prevent adding flights in the past
		LocalDateTime now = LocalDateTime.now();
		if (!request.getDepartureTime().isAfter(now)) {
			log.warn("Attempted to add flight with past departure time. Departure: {}, Now: {}",
					request.getDepartureTime(), now);
			return Mono.error(new ApiException("Cannot add flight with past departure time"));
		}

		// Check if same flight already exists
		return flightRepository
				.findByFlightNumberAndDepartureTime(request.getFlightNumber(), request.getDepartureTime())
				.flatMap(existing -> {
					log.warn("Duplicate flight found: flightNumber={}, departure={}", request.getFlightNumber(),
							request.getDepartureTime());
					return Mono.error(new ApiException("Flight already exists with same flight number"));
				}).switchIfEmpty(Mono.defer(() -> {

					log.info("Flight does not exist, proceeding to create a new one");

					// Find airline OR create a new one if not exists
					Mono<Airline> airlineMono = airlineRepository.findByName(request.getAirlineName())
							.doOnNext(a -> log.debug("Existing airline found: {}", a.getName()))
							.switchIfEmpty(Mono.defer(() -> {
								log.info("Airline not found, creating new airline: {}", request.getAirlineName());
								return airlineRepository
										.save(Airline.builder().name(request.getAirlineName())
												.logoUrl(request.getAirlineLogoUrl()).build())
										.doOnSuccess(a -> log.info("New airline saved with id {}", a.getId()));
							}));

					// After we get the airline, create a flight
					return airlineMono.flatMap(airline -> {

						Flight flight = Flight.builder().flightNumber(request.getFlightNumber())
								.fromPlace(request.getFromPlace()).toPlace(request.getToPlace())
								.departureTime(request.getDepartureTime()).arrivalTime(request.getArrivalTime())
								.price(request.getPrice()).totalSeats(request.getTotalSeats())
								.availableSeats(request.getTotalSeats()).airlineId(airline.getId()).build();

						log.debug("Saving new flight for airline {}: {}", airline.getName(), flight);

						return flightRepository.save(flight)
								.doOnSuccess(f -> log.info("Flight saved successfully with id {}", f.getId()));
					});
				})).cast(Flight.class);
	}

	@Override
	public Flux<Flight> searchFlights(FlightSearchRequest req) {

		log.info("Searching flights from {} to {} on {}", req.getFromPlace(), req.getToPlace(), req.getTravelDate());

		// Converting the LocalDate into a start & end range.
		LocalDate date = req.getTravelDate();
		LocalDateTime start = date.atStartOfDay();
		LocalDateTime end = date.atTime(23, 59, 59);

		// This calls my method in FlightRepository.
		return flightRepository
				.findByFromPlaceAndToPlaceAndDepartureTimeBetween(req.getFromPlace(), req.getToPlace(), start, end)
				.doOnComplete(() -> log.info("Flight search completed"))
				.doOnError(ex -> log.error("Error during flight search: {}", ex.getMessage()));
	}

	@Override
	public Mono<Flight> getFlightById(String id) {

		log.info("Fetching flight by id {}", id);

		return flightRepository.findById(id).doOnNext(f -> log.debug("Flight found: {}", f))
				.switchIfEmpty(Mono.error(new ApiException("Flight not found: " + id)))
				.doOnError(ex -> log.error("Error fetching flight {}: {}", id, ex.getMessage()));
	}
}
