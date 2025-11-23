package com.flightapp.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

// This class represents a booking. 

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "bookings")
public class Booking {

	@Id
	String id; // Mongo ID

	// PNR is manually generated (UUID trimmed)
	String pnr;
	String email;

	// Linking to flight using flightId (string)
	String flightId;

	// number of seats booked
	Integer seatsBooked;

	LocalDateTime bookingTime;

	// If user cancels ticket, I mark canceled=true.
	Boolean canceled;
	LocalDateTime canceledAt;
}
