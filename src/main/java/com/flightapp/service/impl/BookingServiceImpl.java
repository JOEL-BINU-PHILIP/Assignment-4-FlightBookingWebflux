package com.flightapp.service.impl;

import com.flightapp.dto.*;
import com.flightapp.entity.Booking;
import com.flightapp.entity.Passenger;
import com.flightapp.repository.BookingRepository;
import com.flightapp.repository.FlightRepository;
import com.flightapp.repository.PassengerRepository;
import com.flightapp.service.BookingService;
import com.flightapp.exception.ApiException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

// Booking is the most complex logic in the app,
// because I'm updating multiple collections: bookings, passengers, flights.
@Service
@Slf4j
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

		log.info("Booking request received for flightId: {}", flightId);
		log.debug("BookingRequest payload: {}", request);

		// Step 1: Check if flight exists
		return flightRepository.findById(flightId)
				.doOnSubscribe(s -> log.info("Checking flight availability for flightId: {}", flightId))
				.switchIfEmpty(Mono.error(new ApiException("Flight not found")))
				.doOnNext(f -> log.debug("Flight found: {}", f)).flatMap(flight -> {

					LocalDateTime now = LocalDateTime.now();

					// 🚫 NEW VALIDATION: Prevent booking flights in the past
					if (!flight.getDepartureTime().isAfter(now)) {
						log.warn("Attempted booking for a past flight. Departure: {}, Now: {}",
								flight.getDepartureTime(), now);
						return Mono.error(new ApiException("Cannot book a ticket for a past flight"));
					}

					// Step 2: Validate seat counts
					if (flight.getAvailableSeats() < request.getNumberOfSeats()) {
						log.warn("Not enough seats available. Requested: {}, Available: {}", request.getNumberOfSeats(),
								flight.getAvailableSeats());
						return Mono.error(new ApiException("Not enough seats available"));
					}

					// Step 3: Passenger count must match seats
					if (request.getPassengers() == null
							|| request.getPassengers().size() != request.getNumberOfSeats()) {

						log.warn("Passenger count mismatch. Expected: {}, Actual: {}", request.getNumberOfSeats(),
								request.getPassengers() == null ? 0 : request.getPassengers().size());

						return Mono.error(new ApiException("Passenger list size must equal numberOfSeats"));
					}

					// Step 4: Ensure unique seat numbers
					long distinctSeats = request.getPassengers().stream().map(PassengerRequest::getSeatNumber)
							.distinct().count();

					if (distinctSeats != request.getPassengers().size()) {
						log.warn("Duplicate seat numbers detected in booking request");
						return Mono.error(new ApiException("Duplicate seat numbers in request"));
					}

					// Generate PNR (I used a trimmed UUID)
					String pnr = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
					log.info("Generated PNR: {}", pnr);

					Booking booking = Booking.builder().pnr(pnr).email(request.getEmail()).flightId(flight.getId())
							.seatsBooked(request.getNumberOfSeats()).bookingTime(LocalDateTime.now()).canceled(false)
							.build();

					// Step 5: Save booking
					return bookingRepository.save(booking)
							.doOnSuccess(b -> log.info("Booking saved in DB with id: {}", b.getId()))
							.flatMap(savedBooking -> {

								// Step 6: Convert PassengerRequest → Passenger entity
								var passengerDocs = request.getPassengers().stream()
										.map(p -> Passenger.builder().name(p.getName()).gender(p.getGender())
												.age(p.getAge()).meal(p.getMeal()).seatNumber(p.getSeatNumber())
												.bookingId(savedBooking.getId()).build())
										.collect(Collectors.toList());

								log.debug("Passenger entities created: {}", passengerDocs);

								// Step 7: Save passengers
								return passengerRepository.saveAll(passengerDocs).collectList()
										.doOnSuccess(list -> log.info("Saved {} passengers", list.size()))
										.flatMap(savedPassengers -> {

											// Step 8: Reduce available seats
											flight.setAvailableSeats(
													flight.getAvailableSeats() - request.getNumberOfSeats());
											log.info("Updated available seats for flight {} -> {}", flight.getId(),
													flight.getAvailableSeats());

											// Step 9: Save updated flight and return response
											return flightRepository.save(flight)
													.doOnSuccess(
															f -> log.info("Flight seat count updated successfully"))
													.thenReturn(toResponse(savedBooking, savedPassengers));
										});
							});
				}).doOnError(ex -> log.error("Error while booking ticket: {}", ex.getMessage()));
	}

	@Override
	public Mono<BookingResponse> getTicketByPnr(String pnr) {

		log.info("Fetching ticket for PNR: {}", pnr);

		// Fetch booking + passengers and merge them into one response
		return bookingRepository.findByPnr(pnr).switchIfEmpty(Mono.error(new ApiException("PNR not found")))
				.doOnNext(b -> log.info("Booking found for PNR {}", pnr))
				.flatMap(booking -> passengerRepository.findByBookingId(booking.getId()).collectList()
						.doOnSuccess(
								list -> log.debug("Fetched {} passengers for booking {}", list.size(), booking.getId()))
						.map(passengers -> toResponse(booking, passengers)))
				.doOnError(ex -> log.error("Error fetching ticket for PNR {}: {}", pnr, ex.getMessage()));
	}

	@Override
	public Flux<BookingResponse> getBookingHistory(String email) {

		log.info("Fetching booking history for email: {}", email);

		// Returns list of bookings merged with passengers
		return bookingRepository.findByEmail(email).doOnNext(b -> log.debug("Processing booking id: {}", b.getId()))
				.flatMap(booking -> passengerRepository.findByBookingId(booking.getId()).collectList()
						.map(list -> toResponse(booking, list)))
				.doOnComplete(() -> log.info("Completed fetching booking history for {}", email))
				.doOnError(ex -> log.error("Error fetching booking history for {}: {}", email, ex.getMessage()));
	}

	@Override
	public Mono<Void> cancelBooking(String pnr) {

		log.info("Received cancellation request for PNR: {}", pnr);

		// Fetch booking first
		return bookingRepository.findByPnr(pnr).switchIfEmpty(Mono.error(new ApiException("PNR not found")))
				.doOnNext(b -> log.debug("Found booking {} for cancellation", b.getId())).flatMap(booking -> {

					if (Boolean.TRUE.equals(booking.getCanceled())) {
						log.warn("Booking already cancelled for PNR: {}", pnr);
						return Mono.error(new ApiException("Already canceled"));
					}

					// Fetch the flight to validate 24-hour rule
					return flightRepository.findById(booking.getFlightId())
							.doOnNext(f -> log.debug("Checking cancellation rules for flight {}", f.getId()))
							.flatMap(flight -> {

								LocalDateTime now = LocalDateTime.now();

								// VALIDATION: Flight already departed
								if (!flight.getDepartureTime().isAfter(now)) {
									log.warn("Cancellation refused: flight {} already departed at {}", flight.getId(),
											flight.getDepartureTime());
									return Mono.error(new ApiException("Cannot cancel — flight already departed"));
								}

								// VALIDATION: Within 24 hours
								if (!flight.getDepartureTime().isAfter(now.plusHours(24))) {
									log.warn("Cancellation refused: flight {} departs within 24 hours at {}",
											flight.getId(), flight.getDepartureTime());
									return Mono.error(new ApiException("Cannot cancel within 24 hours of departure"));
								}

								// Mark canceled
								booking.setCanceled(true);
								booking.setCanceledAt(LocalDateTime.now());

								log.info("Marking booking {} as canceled", booking.getId());

								return bookingRepository.save(booking)
										.doOnSuccess(b -> log.info("Booking canceled in DB")).flatMap(saved -> {

											// Add seats back after cancellation
											flight.setAvailableSeats(
													flight.getAvailableSeats() + booking.getSeatsBooked());

											log.info("Re-added {} seats to flight {}", booking.getSeatsBooked(),
													flight.getId());

											return flightRepository.save(flight).doOnSuccess(
													f -> log.info("Flight updated successfully after cancellation"))
													.then();
										});
							});
				}).doOnError(ex -> log.error("Error canceling booking for PNR {}: {}", pnr, ex.getMessage()));
	}

	// Helper method to convert Booking + Passenger list → BookingResponse DTO
	private BookingResponse toResponse(Booking booking, java.util.List<Passenger> passengers) {

		log.debug("Converting booking {} + {} passengers into response", booking.getId(), passengers.size());

		var passengerDtos = passengers.stream().map(p -> PassengerRequest.builder().name(p.getName())
				.gender(p.getGender()).age(p.getAge()).meal(p.getMeal()).seatNumber(p.getSeatNumber()).build())
				.collect(Collectors.toList());

		return BookingResponse.builder().pnr(booking.getPnr()).email(booking.getEmail()).flightId(booking.getFlightId())
				.seatsBooked(booking.getSeatsBooked()).bookingTime(booking.getBookingTime())
				.canceled(booking.getCanceled()).passengers(passengerDtos).build();
	}
}
