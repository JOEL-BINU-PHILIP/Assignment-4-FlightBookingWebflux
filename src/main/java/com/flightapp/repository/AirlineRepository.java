package com.flightapp.repository;

import com.flightapp.entity.Airline;
//Using ReactiveMongoRepository.
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

// This repository talks to the "airlines" collection in MongoDB.
// Just extend the interface & all CRUD works.

public interface AirlineRepository extends ReactiveMongoRepository<Airline, String> {

	// I added this custom finder method so I can check if an airline already exists
	// while adding inventory.
	Mono<Airline> findByName(String name);
}
