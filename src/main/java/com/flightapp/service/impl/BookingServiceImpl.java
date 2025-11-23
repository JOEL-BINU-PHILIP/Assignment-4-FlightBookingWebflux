package com.flightapp.service.impl;

import com.flightapp.dto.*;
import com.flightapp.entity.Booking;
import com.flightapp.entity.Flight;
import com.flightapp.entity.Passenger;
import com.flightapp.repository.BookingRepository;
import com.flightapp.repository.FlightRepository;
import com.flightapp.repository.PassengerRepository;
import com.flightapp.service.BookingService;
import com.flightapp.exception.ApiException;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

// Booking is the most complex logic in the app,
// because I'm updating multiple collections: bookings, passengers, flights.
@Service
public class BookingServiceImpl implements BookingService {

	private final BookingRepository bookingRepository;
	private final FlightRepository flightRepository;
	private final PassengerRepository passengerRepository;

	public BookingServiceImpl(BookingRepository bookingRepository, FlightRepository flightRepository,
			PassengerRepository passengerRepository) {
		this.bookingRepository = bookingRepository;
		this.flightRepository = flightRepository;
		this.passengerRepository = passengerRepository;
	}

	@Override
	public Mono<BookingResponse> bookTicket(String flightId, BookingRequest request) {

		// Step 1: Check if flight exists
		return flightRepository.findById(flightId).switchIfEmpty(Mono.error(new ApiException("Flight not found")))
				.flatMap(flight -> {

					// Step 2: Validate seat counts
					if (flight.getAvailableSeats() < request.getNumberOfSeats()) {
						return Mono.error(new ApiException("Not enough seats available"));
					}

					// Step 3: Passenger count must match seats
					if (request.getPassengers() == null
							|| request.getPassengers().size() != request.getNumberOfSeats()) {
						return Mono.error(new ApiException("Passenger list size must equal numberOfSeats"));
					}

					// Step 4: Ensure unique seat numbers
					long distinctSeats = request.getPassengers().stream().map(PassengerRequest::getSeatNumber)
							.distinct().count();

					if (distinctSeats != request.getPassengers().size()) {
						return Mono.error(new ApiException("Duplicate seat numbers in request"));
					}

					// Generate PNR (I used a trimmed UUID)
					String pnr = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

					Booking booking = Booking.builder().pnr(pnr).email(request.getEmail()).flightId(flight.getId())
							.seatsBooked(request.getNumberOfSeats()).bookingTime(LocalDateTime.now()).canceled(false)
							.build();

					// Step 5: Save booking
					return bookingRepository.save(booking).flatMap(savedBooking -> {

						// Step 6: Convert PassengerRequest → Passenger entity
						var passengerDocs = request.getPassengers().stream()
								.map(p -> Passenger.builder().name(p.getName()).gender(p.getGender()).age(p.getAge())
										.meal(p.getMeal()).seatNumber(p.getSeatNumber()).bookingId(savedBooking.getId())
										.build())
								.collect(Collectors.toList());

						// Step 7: Save passengers
						return passengerRepository.saveAll(passengerDocs).collectList().flatMap(savedPassengers -> {

							// Step 8: Reduce available seats
							flight.setAvailableSeats(flight.getAvailableSeats() - request.getNumberOfSeats());

							// Step 9: Save updated flight and return response
							return flightRepository.save(flight).thenReturn(toResponse(savedBooking, savedPassengers));
						});
					});
				});
	}

	@Override
	public Mono<BookingResponse> getTicketByPnr(String pnr) {

		// Fetch booking + passengers and merge them into one response
		return bookingRepository.findByPnr(pnr).switchIfEmpty(Mono.error(new ApiException("PNR not found")))
				.flatMap(booking -> passengerRepository.findByBookingId(booking.getId()).collectList()
						.map(passengers -> toResponse(booking, passengers)));
	}

	@Override
	public Flux<BookingResponse> getBookingHistory(String email) {

		// Returns list of bookings merged with passengers
		return bookingRepository.findByEmail(email).flatMap(booking -> passengerRepository
				.findByBookingId(booking.getId()).collectList().map(list -> toResponse(booking, list)));
	}

	@Override
	public Mono<Void> cancelBooking(String pnr) {

		// Fetch booking first
		return bookingRepository.findByPnr(pnr).switchIfEmpty(Mono.error(new ApiException("PNR not found")))
				.flatMap(booking -> {

					if (Boolean.TRUE.equals(booking.getCanceled())) {
						return Mono.error(new ApiException("Already canceled"));
					}

					// Fetch the flight to validate 24-hour rule
					return flightRepository.findById(booking.getFlightId()).flatMap(flight -> {

						LocalDateTime now = LocalDateTime.now();
						if (!flight.getDepartureTime().isAfter(now.plusHours(24))) {
							return Mono.error(new ApiException("Cannot cancel within 24 hours of departure"));
						}

						// Mark canceled
						booking.setCanceled(true);
						booking.setCanceledAt(LocalDateTime.now());

						return bookingRepository.save(booking).flatMap(saved -> {

							// Add seats back after cancellation
							flight.setAvailableSeats(flight.getAvailableSeats() + booking.getSeatsBooked());

							return flightRepository.save(flight).then();
						});
					});
				});
	}

	// Helper method to convert Booking + Passenger list → BookingResponse DTO
	private BookingResponse toResponse(Booking booking, java.util.List<Passenger> passengers) {

		var passengerDtos = passengers.stream().map(p -> PassengerRequest.builder().name(p.getName())
				.gender(p.getGender()).age(p.getAge()).meal(p.getMeal()).seatNumber(p.getSeatNumber()).build())
				.collect(Collectors.toList());

		return BookingResponse.builder().pnr(booking.getPnr()).email(booking.getEmail()).flightId(booking.getFlightId())
				.seatsBooked(booking.getSeatsBooked()).bookingTime(booking.getBookingTime())
				.canceled(booking.getCanceled()).passengers(passengerDtos).build();
	}
}
