package com.flightapp.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

// This DTO is used when adding inventory (admin side).
// Basically everything needed to create a flight.

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightInventoryRequest {

	@NotBlank
	@Pattern(regexp = "^[A-Z0-9]{3,10}$", message = "Flight number must be 3–10 characters (uppercase letters or digits)")
	private String flightNumber;

	@NotBlank
	private String fromPlace;

	@NotBlank
	private String toPlace;

	// Flight timings — I'm using LocalDateTime since it works fine with Mongo &
	// Jackson.
	@NotNull
	private LocalDateTime departureTime;

	@NotNull
	private LocalDateTime arrivalTime;

	@NotNull
	private Double price;

	@NotNull
	@Min(1)
	private Integer totalSeats;

	@NotBlank
	private String airlineName;

	private String airlineLogoUrl; // optional
}
