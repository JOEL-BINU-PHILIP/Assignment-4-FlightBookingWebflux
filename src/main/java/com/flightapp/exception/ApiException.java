package com.flightapp.exception;

// This is my custom exception class.
// I throw this whenever I want to show a proper error message to the user.
// Using a custom exception makes the GlobalErrorHandler catch it easily.

public class ApiException extends RuntimeException {

	// Just passing the message to the parent RuntimeException
	public ApiException(String message) {
		super(message);
	}
}
