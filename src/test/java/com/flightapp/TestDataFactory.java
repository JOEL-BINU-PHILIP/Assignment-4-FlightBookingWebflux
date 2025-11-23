package com.flightapp;

import com.flightapp.dto.*;
import com.flightapp.entity.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class TestDataFactory {

	private TestDataFactory() {
	}

	public static Airline sampleAirline() {
		return Airline.builder().id("airline-1").name("Air India").logoUrl("https://airindia/logo.png").build();
	}

	public static FlightInventoryRequest sampleFlightInventoryRequest() {
		return FlightInventoryRequest.builder().flightNumber("AI101").fromPlace("Bangalore").toPlace("Mumbai")
				.departureTime(LocalDateTime.now().plusDays(5).withHour(10).withMinute(30).withSecond(0).withNano(0))
				.arrivalTime(LocalDateTime.now().plusDays(5).withHour(12).withMinute(45).withSecond(0).withNano(0))
				.price(4500f).totalSeats(120).airlineName("Air India").airlineLogoUrl("https://airindia/logo.png")
				.build();
	}

	public static Flight sampleFlight() {
		var dep = LocalDateTime.now().plusDays(5).withHour(10).withMinute(30).withSecond(0).withNano(0);
		return Flight.builder().id("flight-1").flightNumber("AI101").fromPlace("Bangalore").toPlace("Mumbai")
				.departureTime(dep).arrivalTime(dep.plusHours(2).plusMinutes(15)).price(4500f).totalSeats(120)
				.availableSeats(120).airlineId("airline-1").build();
	}

	public static PassengerRequest samplePassengerRequest(String seat) {
		return PassengerRequest.builder().name("Test Passenger").gender("M").age(30).seatNumber(seat).meal("VEG")
				.build();
	}

	public static BookingRequest sampleBookingRequest() {
		return BookingRequest.builder().email("user@example.com").numberOfSeats(2)
				.passengers(List.of(samplePassengerRequest("1A"), samplePassengerRequest("1B"))).build();
	}

	public static Passenger samplePassenger(String bookingId, String seat) {
		return Passenger.builder().id("p-" + seat).name("Test Passenger").gender("M").age(30).seatNumber(seat)
				.meal("VEG").bookingId(bookingId).build();
	}

	public static Booking sampleBooking(String flightId) {
		return Booking.builder().id("booking-1").pnr("PNR12345").email("user@example.com").flightId(flightId)
				.seatsBooked(2).bookingTime(LocalDateTime.now()).canceled(false).build();
	}

	public static BookingResponse sampleBookingResponse(String flightId) {
		return BookingResponse.builder().pnr("PNR12345").email("user@example.com").flightId(flightId).seatsBooked(2)
				.bookingTime(LocalDateTime.now()).canceled(false)
				.passengers(List.of(samplePassengerRequest("1A"), samplePassengerRequest("1B"))).build();
	}

	public static FlightSearchRequest sampleFlightSearchRequest() {
		return FlightSearchRequest.builder().fromPlace("Bangalore").toPlace("Mumbai")
				.travelDate(LocalDate.now().plusDays(5)).oneWay(true).build();
	}
}
