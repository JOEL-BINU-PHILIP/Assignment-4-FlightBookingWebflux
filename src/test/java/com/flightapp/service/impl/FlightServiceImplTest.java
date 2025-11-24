package com.flightapp.service.impl;

import com.flightapp.TestDataFactory;
import com.flightapp.dto.FlightInventoryRequest;
import com.flightapp.dto.FlightSearchRequest;
import com.flightapp.entity.Airline;
import com.flightapp.entity.Flight;
import com.flightapp.exception.ApiException;
import com.flightapp.repository.AirlineRepository;
import com.flightapp.repository.FlightRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class FlightServiceImplTest {

	private FlightRepository flightRepository;
	private AirlineRepository airlineRepository;
	private FlightServiceImpl flightService;

	@BeforeEach
	void setup() {
		flightRepository = mock(FlightRepository.class);
		airlineRepository = mock(AirlineRepository.class);

		flightService = new FlightServiceImpl(flightRepository, airlineRepository);
	}

	// --------------------------------------------------------
	// 1) POSITIVE CASE — Add Inventory
	// --------------------------------------------------------
	@Test
	void testAddInventory_success() {

		FlightInventoryRequest req = TestDataFactory.sampleInventoryRequest();

		// change departure to far future so validation never fails
		req.setDepartureTime(LocalDateTime.now().plusDays(10));
		req.setArrivalTime(LocalDateTime.now().plusDays(10).plusHours(2));

		Airline airline = Airline.builder().id("airline-1").name("Air India").logoUrl("logo.png").build();

		Flight saved = Flight.builder().id("flight-1").flightNumber(req.getFlightNumber()).fromPlace(req.getFromPlace())
				.toPlace(req.getToPlace()).departureTime(req.getDepartureTime()).arrivalTime(req.getArrivalTime())
				.price(req.getPrice()).totalSeats(req.getTotalSeats()).availableSeats(req.getTotalSeats())
				.airlineId("airline-1").build();

		// 1) no duplicate
		when(flightRepository.findByFlightNumberAndDepartureTime(req.getFlightNumber(), req.getDepartureTime()))
				.thenReturn(Mono.empty());

		// 2) airline exists
		when(airlineRepository.findByName(req.getAirlineName())).thenReturn(Mono.just(airline));

		// 3) airline save (must be mocked)
		when(airlineRepository.save(any())).thenReturn(Mono.just(airline));

		// 4) flight save
		when(flightRepository.save(any())).thenReturn(Mono.just(saved));

		StepVerifier.create(flightService.addInventory(req)).expectNext(saved).verifyComplete();
	}

	// --------------------------------------------------------
	// 2) NEGATIVE CASE — Duplicate flight
	// --------------------------------------------------------
	@Test
	void testAddInventory_duplicateFlight() {

		FlightInventoryRequest req = TestDataFactory.sampleInventoryRequest();
		req.setDepartureTime(LocalDateTime.now().plusDays(10)); // ensure future date

		Flight existing = TestDataFactory.sampleFlight();

		when(flightRepository.findByFlightNumberAndDepartureTime(req.getFlightNumber(), req.getDepartureTime()))
				.thenReturn(Mono.just(existing));

		StepVerifier.create(flightService.addInventory(req))
				.expectErrorMatches(ex -> ex instanceof ApiException && ex.getMessage().contains("already exists"))
				.verify();
	}

	// --------------------------------------------------------
	// 3) VALIDATION — Departure after arrival
	// --------------------------------------------------------
	@Test
	void testAddInventory_departureAfterArrival() {

		FlightInventoryRequest req = TestDataFactory.sampleInventoryRequest();

		req.setDepartureTime(LocalDateTime.now().plusDays(10).plusHours(5));
		req.setArrivalTime(LocalDateTime.now().plusDays(10).plusHours(1));

		StepVerifier.create(flightService.addInventory(req)).expectError(ApiException.class).verify();
	}

	// --------------------------------------------------------
	// 4) VALIDATION — Price must be > 0
	// --------------------------------------------------------
	@Test
	void testAddInventory_invalidPrice() {

		FlightInventoryRequest req = TestDataFactory.sampleInventoryRequest();
		req.setDepartureTime(LocalDateTime.now().plusDays(5));
		req.setArrivalTime(req.getDepartureTime().plusHours(2));

		req.setPrice(0f);

		StepVerifier.create(flightService.addInventory(req)).expectError(ApiException.class).verify();
	}

	// --------------------------------------------------------
	// 5) VALIDATION — Seats must be > 0
	// --------------------------------------------------------
	@Test
	void testAddInventory_invalidSeatCount() {

		FlightInventoryRequest req = TestDataFactory.sampleInventoryRequest();
		req.setDepartureTime(LocalDateTime.now().plusDays(5));
		req.setArrivalTime(req.getDepartureTime().plusHours(2));

		req.setTotalSeats(0);

		StepVerifier.create(flightService.addInventory(req)).expectError(ApiException.class).verify();
	}

	// --------------------------------------------------------
	// 6) VALIDATION — Past departure time not allowed
	// --------------------------------------------------------
	@Test
	void testAddInventory_pastDepartureTime() {

		FlightInventoryRequest req = TestDataFactory.sampleInventoryRequest();

		req.setDepartureTime(LocalDateTime.now().minusDays(1));
		req.setArrivalTime(LocalDateTime.now().minusDays(1).plusHours(2));

		StepVerifier.create(flightService.addInventory(req))
				.expectErrorMatches(ex -> ex instanceof ApiException && ex.getMessage().contains("past departure"))
				.verify();
	}

	// --------------------------------------------------------
	// 7) SEARCH FLIGHTS — Positive Case
	// --------------------------------------------------------
	@Test
	void testSearchFlights_success() {

		FlightSearchRequest req = TestDataFactory.sampleSearchRequest();
		Flight f = TestDataFactory.sampleFlight();

		when(flightRepository.findByFromPlaceAndToPlaceAndDepartureTimeBetween(anyString(), anyString(), any(), any()))
				.thenReturn(Flux.just(f));

		StepVerifier.create(flightService.searchFlights(req)).expectNext(f).verifyComplete();
	}

	// --------------------------------------------------------
	// 8) GET FLIGHT BY ID — Success
	// --------------------------------------------------------
	@Test
	void testGetFlightById_success() {

		Flight f = TestDataFactory.sampleFlight();

		when(flightRepository.findById("flight-1")).thenReturn(Mono.just(f));

		StepVerifier.create(flightService.getFlightById("flight-1")).expectNext(f).verifyComplete();
	}

	// --------------------------------------------------------
	// 9) GET FLIGHT BY ID — Not Found
	// --------------------------------------------------------
	@Test
	void testGetFlightById_notFound() {

		when(flightRepository.findById("flight-1")).thenReturn(Mono.empty());

		StepVerifier.create(flightService.getFlightById("flight-1"))
				.expectErrorMatches(ex -> ex instanceof ApiException && ex.getMessage().contains("Flight not found"))
				.verify();
	}
}
