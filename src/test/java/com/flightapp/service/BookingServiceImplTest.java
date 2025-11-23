package com.flightapp.service;

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
import com.flightapp.service.impl.BookingServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class BookingServiceImplTest {

	@Mock
	BookingRepository bookingRepository;

	@Mock
	FlightRepository flightRepository;

	@Mock
	PassengerRepository passengerRepository;

	@InjectMocks
	BookingServiceImpl bookingService;

	@BeforeEach
	void setup() {
	}

	@Test
	void bookTicket_success_reducesSeats_and_returnsBookingResponse() {
		String flightId = "flight-1";
		Flight flight = TestDataFactory.sampleFlight();
		// ensure flight id matches
		flight.setId(flightId);
		flight.setAvailableSeats(5);

		Booking savedBooking = TestDataFactory.sampleBooking(flightId);
		savedBooking.setId("booking-1");

		BookingRequest req = TestDataFactory.sampleBookingRequest();

		// mocks
		Mockito.when(flightRepository.findById(flightId)).thenReturn(Mono.just(flight));
		Mockito.when(bookingRepository.save(Mockito.any(Booking.class))).thenAnswer(inv -> {
			Booking b = inv.getArgument(0);
			b.setId("booking-1");
			return Mono.just(b);
		});

		// passengerRepository.saveAll returns saved passenger docs
		Mockito.when(passengerRepository.saveAll(Mockito.anyIterable())).thenAnswer(inv -> {
			Iterable<Passenger> iterable = inv.getArgument(0);
			return Flux.fromIterable((List<Passenger>) List.copyOf((List) iterable));
		});

		Mockito.when(flightRepository.save(Mockito.any(Flight.class))).thenReturn(Mono.just(flight));

		StepVerifier.create(bookingService.bookTicket(flightId, req)).assertNext(resp -> {
			assert resp.getSeatsBooked() == 2;
			assert resp.getEmail().equals("user@example.com");
			assert resp.getPnr() != null && !resp.getPnr().isBlank();
		}).verifyComplete();

		Mockito.verify(bookingRepository).save(Mockito.any(Booking.class));
		Mockito.verify(flightRepository).save(Mockito.any(Flight.class));
	}

	@Test
	void bookTicket_fails_whenInsufficientSeats() {
		String flightId = "flight-1";
		Flight flight = TestDataFactory.sampleFlight();
		flight.setId(flightId);
		flight.setAvailableSeats(1);

		BookingRequest req = BookingRequest
				.builder().email("user@example.com").numberOfSeats(2).passengers(List
						.of(TestDataFactory.samplePassengerRequest("1A"), TestDataFactory.samplePassengerRequest("1B")))
				.build();

		Mockito.when(flightRepository.findById(flightId)).thenReturn(Mono.just(flight));

		StepVerifier.create(bookingService.bookTicket(flightId, req)).expectError(ApiException.class).verify();
	}

	@Test
	void getTicketByPnr_returnsBookingResponse() {
		Booking booking = TestDataFactory.sampleBooking("flight-1");
		booking.setId("booking-1");

		Mockito.when(bookingRepository.findByPnr(booking.getPnr())).thenReturn(Mono.just(booking));
		Mockito.when(passengerRepository.findByBookingId(booking.getId()))
				.thenReturn(Flux.just(TestDataFactory.samplePassenger(booking.getId(), "1A"),
						TestDataFactory.samplePassenger(booking.getId(), "1B")));

		StepVerifier.create(bookingService.getTicketByPnr(booking.getPnr())).assertNext(resp -> {
			assert resp.getPnr().equals(booking.getPnr());
			assert resp.getPassengers().size() == 2;
		}).verifyComplete();
	}

	@Test
	void cancelBooking_success_returnsVoid() {
		Booking booking = TestDataFactory.sampleBooking("flight-1");
		booking.setId("booking-1");

		Flight flight = TestDataFactory.sampleFlight();
		flight.setId("flight-1");
		// ensure flight departs after 48 hours
		flight.setDepartureTime(LocalDateTime.now().plusHours(48));
		flight.setAvailableSeats(100);

		Mockito.when(bookingRepository.findByPnr(booking.getPnr())).thenReturn(Mono.just(booking));
		Mockito.when(flightRepository.findById(booking.getFlightId())).thenReturn(Mono.just(flight));
		Mockito.when(bookingRepository.save(Mockito.any(Booking.class)))
				.thenAnswer(inv -> Mono.just(inv.getArgument(0)));
		Mockito.when(flightRepository.save(Mockito.any(Flight.class))).thenReturn(Mono.just(flight));

		StepVerifier.create(bookingService.cancelBooking(booking.getPnr())).verifyComplete();

		Mockito.verify(bookingRepository).save(Mockito.any(Booking.class));
		Mockito.verify(flightRepository).save(Mockito.any(Flight.class));
	}
}
