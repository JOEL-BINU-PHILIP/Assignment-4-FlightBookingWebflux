package com.flightapp.controller;

import com.flightapp.TestDataFactory;
import com.flightapp.dto.FlightInventoryRequest;
import com.flightapp.dto.FlightSearchRequest;
import com.flightapp.entity.Flight;
import com.flightapp.dto.BookingRequest;
import com.flightapp.dto.BookingResponse;
import com.flightapp.service.BookingService;
import com.flightapp.service.FlightService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class FlightControllerTest {

	@Mock
	FlightService flightService;

	@Mock
	BookingService bookingService;

	// build client against controller instance
	WebTestClient client;

	private void initClient() {
		FlightController controller = new FlightController(flightService, bookingService);
		client = WebTestClient.bindToController(controller).build();
	}

	@Test
	void addInventory_returnsCreated() {
		initClient();
		FlightInventoryRequest req = TestDataFactory.sampleFlightInventoryRequest();
		Flight saved = TestDataFactory.sampleFlight();

		Mockito.when(flightService.addInventory(Mockito.any(FlightInventoryRequest.class)))
				.thenReturn(Mono.just(saved));

		client.post().uri("/api/v1.0/flight/airline/inventory/add").contentType(MediaType.APPLICATION_JSON)
				.bodyValue(req).exchange().expectStatus().isCreated().expectBody().jsonPath("$.flightNumber")
				.isEqualTo("AI101").jsonPath("$.id").isEqualTo("flight-1");
	}

	@Test
	void searchFlights_returnsMatches() {
		initClient();
		FlightSearchRequest req = TestDataFactory.sampleFlightSearchRequest();
		Flight f = TestDataFactory.sampleFlight();

		Mockito.when(flightService.searchFlights(Mockito.any(FlightSearchRequest.class))).thenReturn(Flux.just(f));

		client.post().uri("/api/v1.0/flight/search").contentType(MediaType.APPLICATION_JSON).bodyValue(req).exchange()
				.expectStatus().isOk().expectBody().jsonPath("$[0].flightNumber").isEqualTo("AI101");
	}

	@Test
	void bookTicket_returnsCreated() {
		initClient();
		BookingRequest req = TestDataFactory.sampleBookingRequest();
		BookingResponse resp = TestDataFactory.sampleBookingResponse("flight-1");

		Mockito.when(bookingService.bookTicket(Mockito.eq("flight-1"), Mockito.any(BookingRequest.class)))
				.thenReturn(Mono.just(resp));

		client.post().uri("/api/v1.0/flight/booking/flight-1").contentType(MediaType.APPLICATION_JSON).bodyValue(req)
				.exchange().expectStatus().isCreated().expectBody().jsonPath("$.pnr").isEqualTo("PNR12345")
				.jsonPath("$.email").isEqualTo("user@example.com");
	}

	@Test
	void getTicket_returnsBookingResponse() {
		initClient();
		BookingResponse resp = TestDataFactory.sampleBookingResponse("flight-1");

		Mockito.when(bookingService.getTicketByPnr(resp.getPnr())).thenReturn(Mono.just(resp));

		client.get().uri("/api/v1.0/flight/ticket/" + resp.getPnr()).exchange().expectStatus().isOk().expectBody()
				.jsonPath("$.pnr").isEqualTo(resp.getPnr());
	}
}
