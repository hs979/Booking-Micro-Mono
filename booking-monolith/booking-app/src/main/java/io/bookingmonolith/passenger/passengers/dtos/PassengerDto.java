package io.bookingmonolith.passenger.passengers.dtos;

import io.bookingmonolith.passenger.passengers.enums.PassengerType;
import java.util.UUID;

public record PassengerDto(
        UUID id,
        String name,
        String passportNumber,
        PassengerType passengerType,
        int age
) { }