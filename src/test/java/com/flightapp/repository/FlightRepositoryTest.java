package com.flightapp.repository;

import com.flightapp.entity.Flight;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;

public class FlightRepositoryTest {

	private FlightRepository flightRepository;

	@BeforeEach
	void setup() {
		flightRepository = Mockito.mock(FlightRepository.class);
	}

	// ---------------------------------------------------------------------
	// 1) Test findByFlightNumberAndDepartureTime
	// ---------------------------------------------------------------------
	@Test
	void testFindByFlightNumberAndDepartureTime_Found() {
		LocalDateTime dep = LocalDateTime.now().plusDays(1);

		Flight f = Flight.builder().id("1").flightNumber("AI101").departureTime(dep).build();

		Mockito.when(flightRepository.findByFlightNumberAndDepartureTime("AI101", dep)).thenReturn(Mono.just(f));

		StepVerifier.create(flightRepository.findByFlightNumberAndDepartureTime("AI101", dep))
				.expectNextMatches(f2 -> f2.getFlightNumber().equals("AI101")).verifyComplete();
	}

	@Test
	void testFindByFlightNumberAndDepartureTime_NotFound() {
		LocalDateTime dep = LocalDateTime.now().plusDays(1);

		Mockito.when(flightRepository.findByFlightNumberAndDepartureTime("AI101", dep)).thenReturn(Mono.empty());

		StepVerifier.create(flightRepository.findByFlightNumberAndDepartureTime("AI101", dep)).verifyComplete();
	}

	// ---------------------------------------------------------------------
	// 2) Test findByFromPlaceAndToPlaceAndDepartureTimeBetween
	// ---------------------------------------------------------------------
	@Test
	void testSearchFlights_Found() {
		LocalDateTime start = LocalDateTime.now();
		LocalDateTime end = LocalDateTime.now().plusHours(5);

		Flight f1 = Flight.builder().id("1").flightNumber("FL001").build();

		Mockito.when(
				flightRepository.findByFromPlaceAndToPlaceAndDepartureTimeBetween(eq("DEL"), eq("BLR"), any(), any()))
				.thenReturn(Flux.just(f1));

		StepVerifier.create(flightRepository.findByFromPlaceAndToPlaceAndDepartureTimeBetween("DEL", "BLR", start, end))
				.expectNextCount(1).verifyComplete();
	}

	@Test
	void testSearchFlights_Empty() {
		LocalDateTime start = LocalDateTime.now();
		LocalDateTime end = LocalDateTime.now().plusHours(3);

		Mockito.when(
				flightRepository.findByFromPlaceAndToPlaceAndDepartureTimeBetween(eq("DEL"), eq("BLR"), any(), any()))
				.thenReturn(Flux.empty());

		StepVerifier.create(flightRepository.findByFromPlaceAndToPlaceAndDepartureTimeBetween("DEL", "BLR", start, end))
				.verifyComplete();
	}

	// ---------------------------------------------------------------------
	// 3) Test findByFlightNumber
	// ---------------------------------------------------------------------
	@Test
	void testFindByFlightNumber() {
		Flight f = Flight.builder().id("1").flightNumber("IX32").build();

		Mockito.when(flightRepository.findByFlightNumber("IX32")).thenReturn(Flux.just(f));

		StepVerifier.create(flightRepository.findByFlightNumber("IX32"))
				.expectNextMatches(fl -> fl.getFlightNumber().equals("IX32")).verifyComplete();
	}

	@Test
	void testFindByFlightNumber_Empty() {
		Mockito.when(flightRepository.findByFlightNumber("NOPE")).thenReturn(Flux.empty());

		StepVerifier.create(flightRepository.findByFlightNumber("NOPE")).verifyComplete();
	}
}
