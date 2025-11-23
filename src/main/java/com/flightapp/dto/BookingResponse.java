package com.flightapp.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

// This is the response we return after booking a ticket OR when the user looks up a PNR.
// Contains seats booked, passengers, flightId, etc.
// I'm using String flightId because Mongo stores IDs as strings.

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

	private String pnr; // Generated PNR code (UUID trimmed)
	private String email;
	private String flightId; // flight’s Mongo ID
	private Integer seatsBooked;
	private LocalDateTime bookingTime;
	private Boolean canceled;

	// Just returning the passenger details back
	private List<PassengerRequest> passengers;
}
