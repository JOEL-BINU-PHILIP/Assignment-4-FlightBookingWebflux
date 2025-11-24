package com.flightapp.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.support.WebExchangeBindException;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.mockito.Mockito.*;

public class GlobalErrorHandlerTest {

	private GlobalErrorHandler handler;

	@BeforeEach
	void setup() {
		handler = new GlobalErrorHandler();
	}

	// 1) API EXCEPTION — should return BAD_REQUEST + error JSON
	@Test
	void testHandleApiException() {
		ApiException ex = new ApiException("Something went wrong");

		Mono<ResponseEntity<Map<String, String>>> result = handler.handleApiException(ex);

		StepVerifier.create(result).expectNextMatches(
				res -> res.getStatusCode().value() == 400 && res.getBody().get("error").equals("Something went wrong"))
				.verifyComplete();
	}

	// 2) VALIDATION EXCEPTION — simulate WebExchangeBindException
	@Test
	void testHandleValidationErrors() {

		// Create a fake binding exception
		WebExchangeBindException bindException = mock(WebExchangeBindException.class);

		// Fake field error: "email" → "Invalid format"
		FieldError fe = new FieldError("objectName", "email", "Invalid format");

		when(bindException.getFieldErrors()).thenReturn(java.util.List.of(fe));

		Mono<ResponseEntity<Map<String, String>>> result = handler.handleValidation(bindException);

		StepVerifier.create(result).expectNextMatches(
				res -> res.getStatusCode().value() == 400 && res.getBody().get("email").equals("Invalid format"))
				.verifyComplete();
	}

	// 3) GENERIC EXCEPTION — INTERNAL_SERVER_ERROR
	@Test
	void testHandleGenericException() {
		Exception ex = new RuntimeException("Random crash");

		Mono<ResponseEntity<Map<String, String>>> result = handler.handleAll(ex);

		StepVerifier.create(result).expectNextMatches(
				res -> res.getStatusCode().value() == 500 && res.getBody().get("error").equals("Internal server error"))
				.verifyComplete();
	}
}
