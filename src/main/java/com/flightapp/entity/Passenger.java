package com.flightapp.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// This class stores passenger information for each booking.

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "passengers")
public class Passenger {

    @Id
    private String id; // Mongo gives string IDs
    //Data of a person
    private String name;
    private String gender;
    private Integer age;
    private String seatNumber;
    private String meal;

    // Connects the passenger to a booking.
    private String bookingId;
}
