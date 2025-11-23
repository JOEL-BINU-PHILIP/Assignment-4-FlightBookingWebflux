package com.flightapp.service;

import com.flightapp.TestDataFactory;
import com.flightapp.dto.FlightInventoryRequest;
import com.flightapp.entity.Airline;
import com.flightapp.entity.Flight;
import com.flightapp.exception.ApiException;
import com.flightapp.repository.AirlineRepository;
import com.flightapp.repository.FlightRepository;
import com.flightapp.service.impl.FlightServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class FlightServiceImplTest {

	@Mock
	FlightRepository flightRepository;

	@Mock
	AirlineRepository airlineRepository;

	@InjectMocks
	FlightServiceImpl flightService;

	@BeforeEach
	void setup() {
		// MockitoAnnotations.openMocks(this); // handled by extension
	}

	@Test
	void addInventory_success_createsAirlineAndFlight_whenNoDuplicate() {
		FlightInventoryRequest req = TestDataFactory.sampleFlightInventoryRequest();
		Airline savedAirline = TestDataFactory.sampleAirline();
		Flight savedFlight = TestDataFactory.sampleFlight();

		// no duplicate
		Mockito.when(flightRepository.findByFlightNumberAndDepartureTime(req.getFlightNumber(), req.getDepartureTime()))
				.thenReturn(Mono.empty());

		// airline not found -> saved
		Mockito.when(airlineRepository.findByName(req.getAirlineName())).thenReturn(Mono.empty());
		Mockito.when(airlineRepository.save(Mockito.any(Airline.class))).thenReturn(Mono.just(savedAirline));

		Mockito.when(flightRepository.save(Mockito.any(Flight.class))).thenReturn(Mono.just(savedFlight));

		StepVerifier.create(flightService.addInventory(req))
				.expectNextMatches(f -> f.getId().equals(savedFlight.getId()) && f.getFlightNumber().equals("AI101"))
				.verifyComplete();

		Mockito.verify(airlineRepository).save(Mockito.any(Airline.class));
		Mockito.verify(flightRepository).save(Mockito.any(Flight.class));
	}

	@Test
	void addInventory_fails_whenDuplicateExists() {
		FlightInventoryRequest req = TestDataFactory.sampleFlightInventoryRequest();
		Flight existing = TestDataFactory.sampleFlight();

		Mockito.when(flightRepository.findByFlightNumberAndDepartureTime(req.getFlightNumber(), req.getDepartureTime()))
				.thenReturn(Mono.just(existing));

		StepVerifier.create(flightService.addInventory(req)).expectError(ApiException.class).verify();
	}

	@Test
	void getFlightById_notFound_throwsApiException() {
		Mockito.when(flightRepository.findById("nope")).thenReturn(Mono.empty());

		StepVerifier.create(flightService.getFlightById("nope")).expectError(ApiException.class).verify();
	}

	@Test
	void searchFlights_returnsFlux() {
		var req = TestDataFactory.sampleFlightSearchRequest();
		Flight f = TestDataFactory.sampleFlight();

		LocalDateTime start = req.getTravelDate().atStartOfDay();
		LocalDateTime end = req.getTravelDate().atTime(23, 59, 59);

		Mockito.when(flightRepository.findByFromPlaceAndToPlaceAndDepartureTimeBetween(req.getFromPlace(),
				req.getToPlace(), start, end)).thenReturn(Flux.just(f));

		StepVerifier.create(flightService.searchFlights(req))
				.expectNextMatches(ff -> ff.getFlightNumber().equals("AI101")).verifyComplete();
	}
}
