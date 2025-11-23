package com.flightapp.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// This class represents an Airline. In MongoDB,
// instead of @Table we use @Document. 

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "airlines")
public class Airline {

	// MongoDB generates string IDs so I changed from Long to String.
	@Id
	private String id;

	// Airline name and logo.
	private String name;
	private String logoUrl;
}
