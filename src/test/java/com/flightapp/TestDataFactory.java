package com.flightapp;

import com.flightapp.dto.*;
import com.flightapp.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// TestDataFactory
// Simple factory to create sample DTOs and entities used across unit tests.

public class TestDataFactory {

	// ---------- Flight / Inventory ----------
    private static String placeString = "Bangalore";
    private static String placeString2 = "Mumbai";
    
	public static FlightInventoryRequest sampleInventoryRequest() {
		return FlightInventoryRequest.builder().flightNumber("AI101").fromPlace(placeString).toPlace(placeString2)
				.departureTime(LocalDateTime.of(2025, 2, 15, 10, 30)).arrivalTime(LocalDateTime.of(2025, 2, 15, 12, 45))
				.price(4500f).totalSeats(120).airlineName("Air India").airlineLogoUrl("https://airindia.com/logo.png")
				.build();
	}

	public static FlightSearchRequest sampleSearchRequest() {
		return FlightSearchRequest.builder().fromPlace(placeString).toPlace(placeString2)
				.travelDate(LocalDate.of(2025, 2, 15)).oneWay(true).build();
	}

	public static Flight sampleFlight() {
		return Flight.builder().id("flight-1").flightNumber("AI101").fromPlace(placeString).toPlace(placeString2)
				.departureTime(LocalDateTime.of(2025, 2, 15, 10, 30)).arrivalTime(LocalDateTime.of(2025, 2, 15, 12, 45))
				.price(4500f).totalSeats(120).availableSeats(120).airlineId("airline-1").build();
	}

	// ---------- Booking / Passenger ----------

	public static BookingRequest sampleBookingRequest() {
		PassengerRequest p = PassengerRequest.builder().name("John Doe").gender("M").age(30).seatNumber("1A")
				.meal("veg").build();

		return BookingRequest.builder().email("test@example.com").numberOfSeats(1).passengers(List.of(p)).build();
	}

	public static com.flightapp.entity.Booking sampleBooking() {
		return com.flightapp.entity.Booking.builder().id("booking-1").pnr("PNR12345").email("test@example.com")
				.flightId("flight-1").seatsBooked(1).bookingTime(LocalDateTime.now()).canceled(false).build();
	}

	public static Passenger samplePassenger() {
		return Passenger.builder().id("p1").name("John Doe").gender("M").age(30).seatNumber("1A").meal("veg")
				.bookingId("booking-1").build();
	}
}
