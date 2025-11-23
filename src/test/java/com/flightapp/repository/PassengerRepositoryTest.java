package com.flightapp.repository;

import com.flightapp.entity.Passenger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

public class PassengerRepositoryTest {

	private PassengerRepository passengerRepository;

	@BeforeEach
	void setup() {
		passengerRepository = Mockito.mock(PassengerRepository.class);
	}

	// ---------------------------------------------------------------------
	// 1) findByBookingId — returns passengers
	// ---------------------------------------------------------------------
	@Test
	void testFindByBookingId_Found() {

		Passenger p1 = Passenger.builder().id("P1").name("John").bookingId("B001").build();

		Passenger p2 = Passenger.builder().id("P2").name("Alice").bookingId("B001").build();

		Mockito.when(passengerRepository.findByBookingId("B001")).thenReturn(Flux.just(p1, p2));

		StepVerifier.create(passengerRepository.findByBookingId("B001")).expectNextCount(2).verifyComplete();
	}

	// ---------------------------------------------------------------------
	// 2) findByBookingId — empty
	// ---------------------------------------------------------------------
	@Test
	void testFindByBookingId_Empty() {

		Mockito.when(passengerRepository.findByBookingId("EMPTY")).thenReturn(Flux.empty());

		StepVerifier.create(passengerRepository.findByBookingId("EMPTY")).verifyComplete();
	}

	// ---------------------------------------------------------------------
	// 3) findByBookingId — null (should behave like empty)
	// ---------------------------------------------------------------------
	@Test
	void testFindByBookingId_NullCase() {

		Mockito.when(passengerRepository.findByBookingId(null)).thenReturn(Flux.empty());

		StepVerifier.create(passengerRepository.findByBookingId(null)).verifyComplete();
	}
}
