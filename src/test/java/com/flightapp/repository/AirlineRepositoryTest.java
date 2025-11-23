package com.flightapp.repository;

import com.flightapp.entity.Airline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class AirlineRepositoryTest {

	private AirlineRepository airlineRepository;

	@BeforeEach
	void setup() {
		// Mocking reactive Mongo repository
		airlineRepository = Mockito.mock(AirlineRepository.class);
	}

	// -------------------------------------------------------------------------
	// 1) findByName → positive
	// -------------------------------------------------------------------------
	@Test
	void testFindByName_Found() {

		Airline airline = Airline.builder().id("A1").name("Air India").logoUrl("logo.png").build();

		Mockito.when(airlineRepository.findByName("Air India")).thenReturn(Mono.just(airline));

		StepVerifier.create(airlineRepository.findByName("Air India")).expectNext(airline).verifyComplete();
	}

	// -------------------------------------------------------------------------
	// 2) findByName → empty
	// -------------------------------------------------------------------------
	@Test
	void testFindByName_NotFound() {

		Mockito.when(airlineRepository.findByName("UnknownAir")).thenReturn(Mono.empty());

		StepVerifier.create(airlineRepository.findByName("UnknownAir")).verifyComplete();
	}

	// -------------------------------------------------------------------------
	// 3) save() airline (mock save)
	// -------------------------------------------------------------------------
	@Test
	void testSaveAirline() {

		Airline airline = Airline.builder().id("A2").name("IndiGo").logoUrl("indigo.png").build();

		Mockito.when(airlineRepository.save(airline)).thenReturn(Mono.just(airline));

		StepVerifier.create(airlineRepository.save(airline)).expectNext(airline).verifyComplete();
	}

	// -------------------------------------------------------------------------
	// 4) save() failure case (simulate DB failure)
	// -------------------------------------------------------------------------
	@Test
	void testSaveAirline_Failure() {

		Airline airline = Airline.builder().name("Bad Airline").build();

		Mockito.when(airlineRepository.save(airline)).thenReturn(Mono.error(new RuntimeException("DB error")));

		StepVerifier.create(airlineRepository.save(airline)).expectErrorMessage("DB error").verify();
	}
}
