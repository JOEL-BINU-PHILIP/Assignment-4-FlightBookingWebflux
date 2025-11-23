package com.flightapp.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

// This is the Flight document. No relationships,
// just storing airlineId directly.

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "flights")
public class Flight {

	@Id
	private String id; // Mongo ID is a string (ObjectId internally)

	// Basic flight details
	private String flightNumber;
	private String fromPlace;
	private String toPlace;

	private LocalDateTime departureTime;
	private LocalDateTime arrivalTime;

	private Double price;

	// totalSeats is fixed, availableSeats updates whenever booking happens.
	private Integer totalSeats;
	private Integer availableSeats;

	// Instead of foreign key, I'm storing airlineId manually.
	private String airlineId;
}
