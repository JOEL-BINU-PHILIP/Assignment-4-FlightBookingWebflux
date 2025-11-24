package com.flightapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

// This class handles ALL exceptions from the entire project.
// Basically whenever something goes wrong, instead of ugly stack traces,
// we send neat JSON error responses.

@RestControllerAdvice
@Slf4j
public class GlobalErrorHandler {

	// This catches my custom ApiException.
	// I mostly use this for business logic failures like "Not enough seats".
	@ExceptionHandler(ApiException.class)
	public Mono<ResponseEntity<Map<String, String>>> handleApiException(ApiException ex) {

		log.warn("API exception occurred: {}", ex.getMessage());

		return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage())));
	}

	// This is triggered when validation annotations fail.
	// For example @NotBlank, @Email, @Min, etc.
	@ExceptionHandler(WebExchangeBindException.class)
	public Mono<ResponseEntity<Map<String, String>>> handleValidation(WebExchangeBindException ex) {

		Map<String, String> errors = new HashMap<>();

		// Extracting each field error into a map
		ex.getFieldErrors().forEach(fieldError -> {
			errors.put(fieldError.getField(), fieldError.getDefaultMessage());
		});

		log.warn("Validation failed with errors: {}", errors);

		return Mono.just(ResponseEntity.badRequest().body(errors));
	}

	// This is a safety net — catches literally ANY exception that we didn't handle
	// above.
	@ExceptionHandler(Exception.class)
	public Mono<ResponseEntity<Map<String, String>>> handleAll(Exception ex) {

		// I printed the exception so I can debug things while developing.
		// (Replaced printStackTrace with proper logging)
		log.error("Unhandled exception occurred", ex);

		// Returning a generic error message
		return Mono.just(
				ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error")));
	}
}