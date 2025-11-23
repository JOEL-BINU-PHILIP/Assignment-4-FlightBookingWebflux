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
	private String id; // Mongo ID

	// PNR is manually generated (UUID trimmed)
	private String pnr;
	private String email;

	// Linking to flight using flightId (string)
	private String flightId;

	// number of seats booked
	private Integer seatsBooked;

	private LocalDateTime bookingTime;

	// If user cancels ticket, I mark canceled=true.
	private Boolean canceled;
	private LocalDateTime canceledAt;
}
