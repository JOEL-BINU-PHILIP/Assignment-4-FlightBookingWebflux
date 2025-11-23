package com.flightapp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

// This DTO holds ONE passenger’s info.
// If there are multiple passengers, BookingRequest will contain a list of these.

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerRequest {

	@NotBlank(message = "Passenger name cannot be blank")
	private String name;

	@NotBlank(message = "Gender is required")
	private String gender;

	@Min(value = 1, message = "Age must be positive")
	private int age;

	// Example: "12A", "7B"
	@NotBlank(message = "Seat number is required")
	private String seatNumber;

	// veg / non-veg
	@NotBlank(message = "Meal preference is required")
	private String meal;
}
