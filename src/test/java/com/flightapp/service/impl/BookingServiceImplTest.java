package com.flightapp.service.impl;

import com.flightapp.TestDataFactory;
import com.flightapp.dto.BookingRequest;
import com.flightapp.dto.PassengerRequest;
import com.flightapp.entity.Booking;
import com.flightapp.entity.Flight;
import com.flightapp.entity.Passenger;
import com.flightapp.exception.ApiException;
import com.flightapp.repository.BookingRepository;
import com.flightapp.repository.FlightRepository;
import com.flightapp.repository.PassengerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class BookingServiceImplTest {

	private BookingRepository bookingRepository;
	private FlightRepository flightRepository;
	private PassengerRepository passengerRepository;

	private BookingServiceImpl bookingService;

	@BeforeEach
	void setup() {
		bookingRepository = mock(BookingRepository.class);
		flightRepository = mock(FlightRepository.class);
		passengerRepository = mock(PassengerRepository.class);

		bookingService = new BookingServiceImpl(bookingRepository, flightRepository, passengerRepository);
	}

	// -----------------------------------------------------
	// 1) BOOK TICKET — SUCCESS
	// -----------------------------------------------------
	@Test
	void testBookTicket_success() {
		BookingRequest req = TestDataFactory.sampleBookingRequest();
		Flight flight = TestDataFactory.sampleFlight();
		Booking savedBooking = TestDataFactory.sampleBooking();
		List<Passenger> passengers = List.of(TestDataFactory.samplePassenger());

		when(flightRepository.findById(flight.getId())).thenReturn(Mono.just(flight));
		when(bookingRepository.save(any())).thenReturn(Mono.just(savedBooking));
		when(passengerRepository.saveAll(anyList())).thenReturn(Flux.fromIterable(passengers));
		when(flightRepository.save(any())).thenReturn(Mono.just(flight));

		StepVerifier.create(bookingService.bookTicket(flight.getId(), req))
				.expectNextMatches(resp -> resp.getFlightId().equals(flight.getId())).verifyComplete();
	}

	// -----------------------------------------------------
	// 2) BOOK — FLIGHT NOT FOUND
	// -----------------------------------------------------
	@Test
	void testBookTicket_flightNotFound() {
		BookingRequest req = TestDataFactory.sampleBookingRequest();
		when(flightRepository.findById("badId")).thenReturn(Mono.empty());

		StepVerifier.create(bookingService.bookTicket("badId", req)).expectError(ApiException.class).verify();
	}

	// -----------------------------------------------------
	// 3) BOOK — NOT ENOUGH SEATS
	// -----------------------------------------------------
	@Test
	void testBookTicket_notEnoughSeats() {
		BookingRequest req = TestDataFactory.sampleBookingRequest();
		Flight flight = TestDataFactory.sampleFlight();
		flight.setAvailableSeats(0);

		when(flightRepository.findById(flight.getId())).thenReturn(Mono.just(flight));

		StepVerifier.create(bookingService.bookTicket(flight.getId(), req))
				.expectErrorMatches(ex -> ex instanceof ApiException && ex.getMessage().contains("Not enough seats"))
				.verify();
	}

	// -----------------------------------------------------
	// 4) BOOK — PASSENGER COUNT MISMATCH
	// -----------------------------------------------------
	@Test
	void testBookTicket_passengerMismatch() {
		BookingRequest req = TestDataFactory.sampleBookingRequest();
		req.setNumberOfSeats(2); // mismatch

		Flight flight = TestDataFactory.sampleFlight();

		when(flightRepository.findById(flight.getId())).thenReturn(Mono.just(flight));

		StepVerifier.create(bookingService.bookTicket(flight.getId(), req)).expectError(ApiException.class).verify();
	}

	// -----------------------------------------------------
	// 5) BOOK — DUPLICATE SEAT NUMBERS
	// -----------------------------------------------------
	@Test
	void testBookTicket_duplicateSeats() {
		PassengerRequest p1 = PassengerRequest.builder().name("A").gender("M").age(20).seatNumber("1A").meal("veg")
				.build();

		PassengerRequest p2 = PassengerRequest.builder().name("B").gender("M").age(22).seatNumber("1A").meal("veg")
				.build(); // duplicate seat

		BookingRequest req = BookingRequest.builder().email("x@gmail.com").numberOfSeats(2).passengers(List.of(p1, p2))
				.build();

		Flight flight = TestDataFactory.sampleFlight();

		when(flightRepository.findById(flight.getId())).thenReturn(Mono.just(flight));

		StepVerifier.create(bookingService.bookTicket(flight.getId(), req)).expectError(ApiException.class).verify();
	}

	// -----------------------------------------------------
	// 6) GET TICKET BY PNR — SUCCESS
	// -----------------------------------------------------
	@Test
	void testGetTicketByPnr_success() {
		Booking booking = TestDataFactory.sampleBooking();
		Passenger passenger = TestDataFactory.samplePassenger();

		when(bookingRepository.findByPnr("PNR12345")).thenReturn(Mono.just(booking));
		when(passengerRepository.findByBookingId(booking.getId())).thenReturn(Flux.just(passenger));

		StepVerifier.create(bookingService.getTicketByPnr("PNR12345"))
				.expectNextMatches(resp -> resp.getPnr().equals("PNR12345")).verifyComplete();
	}

	// -----------------------------------------------------
	// 7) GET TICKET — NOT FOUND
	// -----------------------------------------------------
	@Test
	void testGetTicketByPnr_notFound() {
		when(bookingRepository.findByPnr("bad")).thenReturn(Mono.empty());

		StepVerifier.create(bookingService.getTicketByPnr("bad")).expectError(ApiException.class).verify();
	}

	// -----------------------------------------------------
	// 8) BOOKING HISTORY
	// -----------------------------------------------------
	@Test
	void testBookingHistory() {
		Booking booking = TestDataFactory.sampleBooking();
		Passenger passenger = TestDataFactory.samplePassenger();

		when(bookingRepository.findByEmail("test@example.com")).thenReturn(Flux.just(booking));

		when(passengerRepository.findByBookingId(booking.getId())).thenReturn(Flux.just(passenger));

		StepVerifier.create(bookingService.getBookingHistory("test@example.com")).expectNextCount(1).verifyComplete();
	}

	// -----------------------------------------------------
	// 9) CANCEL BOOKING — SUCCESS
	// -----------------------------------------------------
	@Test
	void testCancelBooking_success() {
		Booking booking = TestDataFactory.sampleBooking();
		Flight flight = TestDataFactory.sampleFlight();
		flight.setDepartureTime(LocalDateTime.now().plusHours(30)); // more than 24 hours

		when(bookingRepository.findByPnr(booking.getPnr())).thenReturn(Mono.just(booking));

		when(flightRepository.findById(booking.getFlightId())).thenReturn(Mono.just(flight));

		when(bookingRepository.save(any())).thenReturn(Mono.just(booking));
		when(flightRepository.save(any())).thenReturn(Mono.just(flight));

		StepVerifier.create(bookingService.cancelBooking(booking.getPnr())).verifyComplete();
	}

	// -----------------------------------------------------
	// 10) CANCEL — PNR NOT FOUND
	// -----------------------------------------------------
	@Test
	void testCancelBooking_notFound() {
		when(bookingRepository.findByPnr("bad")).thenReturn(Mono.empty());

		StepVerifier.create(bookingService.cancelBooking("bad")).expectError(ApiException.class).verify();
	}

	// -----------------------------------------------------
	// 11) CANCEL — ALREADY CANCELED
	// -----------------------------------------------------
	@Test
	void testCancelBooking_alreadyCanceled() {
		Booking booking = TestDataFactory.sampleBooking();
		booking.setCanceled(true);

		when(bookingRepository.findByPnr(booking.getPnr())).thenReturn(Mono.just(booking));

		StepVerifier.create(bookingService.cancelBooking(booking.getPnr())).expectError(ApiException.class).verify();
	}

	// -----------------------------------------------------
	// 12) CANCEL — WITHIN 24 HOURS (not allowed)
	// -----------------------------------------------------
	@Test
	void testCancelBooking_within24Hours() {
		Booking booking = TestDataFactory.sampleBooking();
		Flight flight = TestDataFactory.sampleFlight();
		flight.setDepartureTime(LocalDateTime.now().plusHours(5)); // too soon

		when(bookingRepository.findByPnr(booking.getPnr())).thenReturn(Mono.just(booking));

		when(flightRepository.findById(booking.getFlightId())).thenReturn(Mono.just(flight));

		StepVerifier.create(bookingService.cancelBooking(booking.getPnr())).expectError(ApiException.class).verify();
	}
}
