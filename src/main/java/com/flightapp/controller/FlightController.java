package com.flightapp.controller;

import com.flightapp.dto.*;
import com.flightapp.entity.Flight;
import com.flightapp.service.BookingService;
import com.flightapp.service.FlightService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// This is the main controller for all flight + booking operations.

@RestController
@RequestMapping("/api/flight")
@Slf4j
public class FlightController {

	private final FlightService flightService;
	private final BookingService bookingService;

	// Constructor injection so it's easier to test later
	public FlightController(FlightService flightService, BookingService bookingService) {
		this.flightService = flightService;
		this.bookingService = bookingService;
	}

	// 1) ADD NEW FLIGHT INVENTORY
	// This endpoint is used by admin to add flights.
	// I return 201 CREATED because it’s the correct HTTP code.
	@PostMapping("/airline/inventory/add")
	public Mono<ResponseEntity<Flight>> addInventory(@Valid @RequestBody Mono<FlightInventoryRequest> reqMono) {

		log.info("Received request to add flight inventory");

		// reqMono is basically the request body as a reactive stream.
		return reqMono.doOnNext(req -> log.debug("Add Inventory Request: {}", req)).flatMap(flightService::addInventory)
				.doOnSuccess(saved -> log.info("Flight inventory added successfully with ID: {}", saved.getId()))
				.map(savedFlight -> ResponseEntity.status(HttpStatus.CREATED).body(savedFlight))
				.doOnError(ex -> log.error("Error adding inventory: {}", ex.getMessage()));
	}

	// 2) SEARCH FLIGHTS
	// Users search for flights by sending fromPlace, toPlace, and travelDate.
	// I return Flux<Flight> because there can be multiple matching flights.
	@PostMapping("/search")
	public Flux<Flight> searchFlights(@Valid @RequestBody Mono<FlightSearchRequest> reqMono) {
		log.info("Received flight search request");

		// flatMapMany converts Mono -> Flux while calling the service.
		return reqMono.doOnNext(req -> log.debug("Flight Search Request: {}", req))
				.flatMapMany(flightService::searchFlights).doOnComplete(() -> log.info("Flight search completed"))
				.doOnError(ex -> log.error("Error searching flights: {}", ex.getMessage()));
	}

	// 3) BOOK TICKET
	// This endpoint books seats on a particular flight ID.
	// The service handles heavy logic like seat checking, saving passengers, etc.
	@PostMapping("/booking/{flightId}")
	public Mono<ResponseEntity<BookingResponse>> bookTicket(@PathVariable String flightId,
			@Valid @RequestBody Mono<BookingRequest> reqMono) {

		log.info("Received booking request for flightId: {}", flightId);

		return reqMono.doOnNext(req -> log.debug("Booking Request: {}", req))
				.flatMap(req -> bookingService.bookTicket(flightId, req))
				.doOnSuccess(resp -> log.info("Booking successful. PNR: {}", resp.getPnr()))
				.map(resp -> ResponseEntity.status(HttpStatus.CREATED).body(resp))
				.doOnError(ex -> log.error("Error booking ticket: {}", ex.getMessage()));
	}

	// 4) GET TICKET BY PNR
	// When the user enters a PNR, we return booking details + passengers.
	@GetMapping("/ticket/{pnr}")
	public Mono<BookingResponse> getTicket(@PathVariable String pnr) {

		log.info("Fetching ticket for PNR: {}", pnr);

		return bookingService.getTicketByPnr(pnr)
				.doOnSuccess(resp -> log.info("Ticket details fetched for PNR: {}", pnr))
				.doOnError(ex -> log.error("Error fetching ticket for PNR {}: {}", pnr, ex.getMessage()));
	}

	// 5) BOOKING HISTORY
	// Shows all the bookings a user has done, using their email.
	@GetMapping("/booking/history/{email}")
	public Flux<BookingResponse> bookingHistory(@PathVariable String email) {

		log.info("Fetching booking history for email: {}", email);

		return bookingService.getBookingHistory(email)
				.doOnComplete(() -> log.info("Fetched booking history for {}", email))
				.doOnError(ex -> log.error("Error fetching booking history for {}: {}", email, ex.getMessage()));
	}

	// 6) CANCEL BOOKING
	// Cancels a booking if it's more than 24 hours before departure.
	// I return HTTP 204 (no content) after successful cancellation.
	@DeleteMapping("/booking/cancel/{pnr}")
	public Mono<ResponseEntity<Object>> cancelBooking(@PathVariable String pnr) {

		log.info("Received cancel request for PNR: {}", pnr);

		return bookingService.cancelBooking(pnr).then(Mono.just(ResponseEntity.noContent().build()))
				.doOnSuccess(v -> log.info("Successfully canceled booking for PNR: {}", pnr))
				.doOnError(ex -> log.error("Error canceling booking for PNR {}: {}", pnr, ex.getMessage()));
	}
}
