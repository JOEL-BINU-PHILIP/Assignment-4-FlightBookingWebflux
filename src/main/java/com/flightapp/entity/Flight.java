package com.flightapp.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

// This is the Flight document. No relationships,
// just storing airlineId directly.

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "flights")
@CompoundIndex(name = "unique_flight_departure", def = "{'flightNumber': 1, 'departureTime': 1}", unique = true)
public class Flight {

	@Id
	private String id;

	private String flightNumber;
	private String fromPlace;
	private String toPlace;

	private LocalDateTime departureTime;
	private LocalDateTime arrivalTime;

	private Float price;
	private Integer totalSeats;
	private Integer availableSeats;

	private String airlineId;
}
