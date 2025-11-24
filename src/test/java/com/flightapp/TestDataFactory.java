package com.flightapp;

import com.flightapp.dto.*;
import com.flightapp.entity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TestDataFactory {

	// ---------- Flight / Inventory ----------
	private static String placeString = "Bangalore";
	private static String placeString2 = "Mumbai";

	// Base future departure always valid
	private static LocalDateTime baseFutureDeparture() {
		return LocalDateTime.now().plusDays(10).withHour(10).withMinute(30);
	}

	// Arrival ALWAYS > departure
	private static LocalDateTime arrivalAfter(LocalDateTime departure) {
		return departure.plusHours(2); // guaranteed valid
	}

	public static FlightInventoryRequest sampleInventoryRequest() {

		LocalDateTime departure = baseFutureDeparture();
		LocalDateTime arrival = arrivalAfter(departure);

		return FlightInventoryRequest.builder().flightNumber("AI101").fromPlace(placeString).toPlace(placeString2)
				.departureTime(departure).arrivalTime(arrival).price(4500f).totalSeats(120).airlineName("Air India")
				.airlineLogoUrl("https://airindia.com/logo.png").build();
	}

	public static FlightSearchRequest sampleSearchRequest() {
		return FlightSearchRequest.builder().fromPlace(placeString).toPlace(placeString2)
				.travelDate(LocalDate.now().plusDays(10)).oneWay(true).build();
	}

	public static Flight sampleFlight() {

		LocalDateTime departure = baseFutureDeparture();
		LocalDateTime arrival = arrivalAfter(departure);

		return Flight.builder().id("flight-1").flightNumber("AI101").fromPlace(placeString).toPlace(placeString2)
				.departureTime(departure).arrivalTime(arrival).price(4500f).totalSeats(120).availableSeats(120)
				.airlineId("airline-1").build();
	}

	// ---------- Booking / Passenger ----------
	public static BookingRequest sampleBookingRequest() {
		PassengerRequest p = PassengerRequest.builder().name("John Doe").gender("M").age(30).seatNumber("1A")
				.meal("veg").build();

		return BookingRequest.builder().email("test@example.com").numberOfSeats(1).passengers(List.of(p)).build();
	}

	public static Booking sampleBooking() {
		return Booking.builder().id("booking-1").pnr("PNR12345").email("test@example.com").flightId("flight-1")
				.seatsBooked(1).bookingTime(LocalDateTime.now()).canceled(false).build();
	}

	public static Passenger samplePassenger() {
		return Passenger.builder().id("p1").name("John Doe").gender("M").age(30).seatNumber("1A").meal("veg")
				.bookingId("booking-1").build();
	}
}
