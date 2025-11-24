package com.flightapp.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

// This DTO is what the user sends when they want to book a ticket.
// Basically contains email + how many seats + passenger details.
// I added validation annotations so that bad data doesn't even reach the service layer.

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {

	// User must enter correct email — validation takes care of that
	@Email(message = "Invalid email format")
	@NotBlank(message = "Email is required")
	private String email;

	// number of seats they want to book
	@Min(1)
	private int numberOfSeats;

	// The list of passengers — must match numberOfSeats
	@Valid
	@NotEmpty(message = "Passenger list cannot be empty")
	private List<PassengerRequest> passengers;
}
