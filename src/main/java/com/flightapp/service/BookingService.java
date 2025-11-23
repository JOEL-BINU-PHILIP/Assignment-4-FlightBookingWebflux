package com.flightapp.service;

//Basically similar to the previous Assignment
import com.flightapp.dto.BookingRequest;
import com.flightapp.dto.BookingResponse;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

// This interface contains all booking-related operations.

public interface BookingService {

	// Book a ticket for a specific flight
	Mono<BookingResponse> bookTicket(String flightId, BookingRequest request);

	// Fetch details of a single ticket
	Mono<BookingResponse> getTicketByPnr(String pnr);

	// Get the booking history of a user (by email)
	Flux<BookingResponse> getBookingHistory(String email);

	// Cancel a booking (only allowed before 24h of flight departure)
	Mono<Void> cancelBooking(String pnr);
}
