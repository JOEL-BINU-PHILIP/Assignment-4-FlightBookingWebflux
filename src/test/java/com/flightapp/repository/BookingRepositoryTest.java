package com.flightapp.repository;

import com.flightapp.entity.Booking;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyString;

public class BookingRepositoryTest {

	private BookingRepository bookingRepository;
    private static String pnr = "PNR12345";
	@BeforeEach
	void setup() {
		bookingRepository = Mockito.mock(BookingRepository.class);
	}

	// ---------------------------------------------------------------------
	// 1) findByPnr
	// ---------------------------------------------------------------------
	@Test
	void testFindByPnr_Found() {
		Booking booking = Booking.builder().id("B1").pnr(pnr).email("test@mail.com").build();

		Mockito.when(bookingRepository.findByPnr(pnr)).thenReturn(Mono.just(booking));

		StepVerifier.create(bookingRepository.findByPnr(pnr))
				.expectNextMatches(b -> b.getPnr().equals(pnr)).verifyComplete();
	}

	@Test
	void testFindByPnr_NotFound() {
		Mockito.when(bookingRepository.findByPnr(anyString())).thenReturn(Mono.empty());

		StepVerifier.create(bookingRepository.findByPnr("WRONG")).verifyComplete();
	}

	// ---------------------------------------------------------------------
	// 2) findByEmail
	// ---------------------------------------------------------------------
	@Test
	void testFindByEmail_Found() {
		Booking b1 = Booking.builder().id("1").email("test@mail.com").build();
		Booking b2 = Booking.builder().id("2").email("test@mail.com").build();

		Mockito.when(bookingRepository.findByEmail("test@mail.com")).thenReturn(Flux.just(b1, b2));

		StepVerifier.create(bookingRepository.findByEmail("test@mail.com")).expectNextCount(2).verifyComplete();
	}

	@Test
	void testFindByEmail_Empty() {
		Mockito.when(bookingRepository.findByEmail("none@mail.com")).thenReturn(Flux.empty());

		StepVerifier.create(bookingRepository.findByEmail("none@mail.com")).verifyComplete();
	}
}
