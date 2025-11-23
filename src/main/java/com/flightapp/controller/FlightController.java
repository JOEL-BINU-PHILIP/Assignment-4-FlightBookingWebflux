package com.flightapp.controller;

import com.flightapp.dto.*;
import com.flightapp.entity.Flight;
import com.flightapp.service.BookingService;
import com.flightapp.service.FlightService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// This is the main controller for all flight + booking operations.

@RestController
@RequestMapping("/api/v1.0/flight")
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

		// reqMono is basically the request body as a reactive stream.
		return reqMono.flatMap(flightService::addInventory)
				.map(savedFlight -> ResponseEntity.status(HttpStatus.CREATED).body(savedFlight));
	}


	// 2) SEARCH FLIGHTS
	// Users search for flights by sending fromPlace, toPlace, and travelDate.
	// I return Flux<Flight> because there can be multiple matching flights.
	@PostMapping("/search")
	public Flux<Flight> searchFlights(@Valid @RequestBody Mono<FlightSearchRequest> reqMono) {

		// flatMapMany converts Mono -> Flux while calling the service.
		return reqMono.flatMapMany(flightService::searchFlights);
	}

	// 3) BOOK TICKET
	// This endpoint books seats on a particular flight ID.
	// The service handles heavy logic like seat checking, saving passengers, etc.
	@PostMapping("/booking/{flightId}")
	public Mono<ResponseEntity<BookingResponse>> bookTicket(@PathVariable String flightId,
			@Valid @RequestBody Mono<BookingRequest> reqMono) {
		return reqMono.flatMap(req -> bookingService.bookTicket(flightId, req))
				.map(resp -> ResponseEntity.status(HttpStatus.CREATED).body(resp));
	}


	// 4) GET TICKET BY PNR
	// When the user enters a PNR, we return booking details + passengers.
	@GetMapping("/ticket/{pnr}")
	public Mono<BookingResponse> getTicket(@PathVariable String pnr) {
		return bookingService.getTicketByPnr(pnr);
	}

	// 5) BOOKING HISTORY
	// Shows all the bookings a user has done, using their email.
	@GetMapping("/booking/history/{email}")
	public Flux<BookingResponse> bookingHistory(@PathVariable String email) {
		return bookingService.getBookingHistory(email);
	}

	// 6) CANCEL BOOKING
	// Cancels a booking if it's more than 24 hours before departure.
	// I return HTTP 204 (no content) after successful cancellation.
	@DeleteMapping("/booking/cancel/{pnr}")
	public Mono<ResponseEntity<Void>> cancelBooking(@PathVariable String pnr) {

		return bookingService.cancelBooking(pnr).thenReturn(ResponseEntity.noContent().<Void>build());
	}
}
