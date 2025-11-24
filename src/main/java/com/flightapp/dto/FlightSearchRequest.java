package com.flightapp.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

// User enters from, to, and date.
// I kept it simple so it matches the assignment exactly.

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightSearchRequest {

	@NotBlank
	private String fromPlace;

	@NotBlank
	private String toPlace;

	@NotNull
	private LocalDate travelDate;

	// oneWay or roundTrip
	private Boolean oneWay = true;
}
