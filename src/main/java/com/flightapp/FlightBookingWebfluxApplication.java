package com.flightapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class FlightBookingWebfluxApplication {

	public static void main(String[] args) {
		log.info("Starting FlightBookingWebfluxApplication...");
		SpringApplication.run(FlightBookingWebfluxApplication.class, args);
		log.info("FlightBookingWebfluxApplication started.");
	}

}