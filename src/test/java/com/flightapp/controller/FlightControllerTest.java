package com.flightapp.controller;

import com.flightapp.TestDataFactory;
import com.flightapp.dto.*;
import com.flightapp.entity.Flight;
import com.flightapp.exception.ApiException;
import com.flightapp.service.BookingService;
import com.flightapp.service.FlightService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class FlightControllerTest {

	private FlightService flightService;
	private BookingService bookingService;
	private WebTestClient webTestClient;
	private static String apiPath = "/api/flight/airline/inventory/add";
	private static String flightString = "flight-1";

	@BeforeEach
	void setup() {
		flightService = mock(FlightService.class);
		bookingService = mock(BookingService.class);

		FlightController controller = new FlightController(flightService, bookingService);

		webTestClient = WebTestClient.bindToController(controller)
				.controllerAdvice(new com.flightapp.exception.GlobalErrorHandler()).build();
	}

	// 1) ADD INVENTORY — SUCCESS
	@Test
	void testAddInventory_success() {
		FlightInventoryRequest req = TestDataFactory.sampleInventoryRequest();
		Flight saved = TestDataFactory.sampleFlight();

		when(flightService.addInventory(any())).thenReturn(Mono.just(saved));

		webTestClient.post().uri(apiPath).contentType(MediaType.APPLICATION_JSON).bodyValue(req).exchange()
				.expectStatus().isCreated().expectBody().jsonPath("$.id").isEqualTo(flightString);
	}

	// 2) ADD INVENTORY — SERVICE THROWS ERROR
	@Test
	void testAddInventory_serviceError() {
		FlightInventoryRequest req = TestDataFactory.sampleInventoryRequest();

		when(flightService.addInventory(any())).thenReturn(Mono.error(new ApiException("Duplicate flight")));

		webTestClient.post().uri(apiPath).contentType(MediaType.APPLICATION_JSON).bodyValue(req).exchange()
				.expectStatus().isBadRequest().expectBody().jsonPath("$.error").isEqualTo("Duplicate flight");
	}

	// 3) ADD INVENTORY — VALIDATION FAILURE
	@Test
	void testAddInventory_validationFailed() {
		FlightInventoryRequest req = TestDataFactory.sampleInventoryRequest();
		req.setFlightNumber(""); // invalid

		webTestClient.post().uri(apiPath).contentType(MediaType.APPLICATION_JSON).bodyValue(req).exchange()
				.expectStatus().isBadRequest().expectBody().jsonPath("$.flightNumber").exists();
	}

	// 4) SEARCH FLIGHTS — SUCCESS
	@Test
	void testSearchFlights_success() {
		Flight flight = TestDataFactory.sampleFlight();
		FlightSearchRequest req = TestDataFactory.sampleSearchRequest();

		when(flightService.searchFlights(any())).thenReturn(Flux.just(flight));

		webTestClient.post().uri("/api/flight/search").contentType(MediaType.APPLICATION_JSON).bodyValue(req)
				.exchange().expectStatus().isOk().expectBody().jsonPath("$[0].flightNumber").isEqualTo("AI101");
	}

	// 5) SEARCH — NO FLIGHTS
	@Test
	void testSearchFlights_noResults() {
		FlightSearchRequest req = TestDataFactory.sampleSearchRequest();

		when(flightService.searchFlights(any())).thenReturn(Flux.empty());

		webTestClient.post().uri("/api/flight/search").contentType(MediaType.APPLICATION_JSON).bodyValue(req)
				.exchange().expectStatus().isOk().expectBody().json("[]");
	}

	// 6) BOOK TICKET — SUCCESS
	@Test
	void testBookTicket_success() {
		BookingRequest req = TestDataFactory.sampleBookingRequest();
		BookingResponse resp = BookingResponse.builder().pnr("ABCD1234").flightId(flightString)
				.email("test@example.com").seatsBooked(1).build();

		when(bookingService.bookTicket(anyString(), any())).thenReturn(Mono.just(resp));

		webTestClient.post().uri("/api/flight/booking/flight-1").contentType(MediaType.APPLICATION_JSON)
				.bodyValue(req).exchange().expectStatus().isCreated().expectBody().jsonPath("$.pnr")
				.isEqualTo("ABCD1234");
	}

	// 7) BOOK TICKET — SERVICE ERROR
	@Test
	void testBookTicket_serviceError() {
		BookingRequest req = TestDataFactory.sampleBookingRequest();

		when(bookingService.bookTicket(anyString(), any()))
				.thenReturn(Mono.error(new ApiException("Not enough seats")));

		webTestClient.post().uri("/api/flight/booking/flight-1").contentType(MediaType.APPLICATION_JSON)
				.bodyValue(req).exchange().expectStatus().isBadRequest().expectBody().jsonPath("$.error")
				.isEqualTo("Not enough seats");
	}

	// 8) GET TICKET BY PNR — SUCCESS
	@Test
	void testGetTicket_success() {
		BookingResponse resp = BookingResponse.builder().pnr("PNR12345").flightId(flightString).build();

		when(bookingService.getTicketByPnr("PNR12345")).thenReturn(Mono.just(resp));

		webTestClient.get().uri("/api/flight/ticket/PNR12345").exchange().expectStatus().isOk().expectBody()
				.jsonPath("$.pnr").isEqualTo("PNR12345");
	}

	// 9) GET TICKET — NOT FOUND
	@Test
	void testGetTicket_notFound() {
		when(bookingService.getTicketByPnr("BAD")).thenReturn(Mono.error(new ApiException("PNR not found")));

		webTestClient.get().uri("/api/flight/ticket/BAD").exchange().expectStatus().isBadRequest().expectBody()
				.jsonPath("$.error").isEqualTo("PNR not found");
	}

	// 10) BOOKING HISTORY — SUCCESS
	@Test
	void testBookingHistory_success() {
		BookingResponse resp = BookingResponse.builder().pnr("PNR00001").flightId(flightString).build();

		when(bookingService.getBookingHistory("test@example.com")).thenReturn(Flux.just(resp));

		webTestClient.get().uri("/api/flight/booking/history/test@example.com").exchange().expectStatus().isOk()
				.expectBody().jsonPath("$[0].pnr").isEqualTo("PNR00001");
	}

	// 11) CANCEL BOOKING — SUCCESS
	@Test
	void testCancelBooking_success() {
		when(bookingService.cancelBooking("PNR12345")).thenReturn(Mono.empty());

		webTestClient.delete().uri("/api/flight/booking/cancel/PNR12345").exchange().expectStatus().isNoContent();
	}
}
