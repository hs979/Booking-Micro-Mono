package io.bookingmonolith.passenger.passengers.features.createpassenger;

import io.bookingmonolith.passenger.passengers.enums.PassengerType;

public record CreatePassengerRequestDto(
        String name,
        String PassportNumber,
        PassengerType passengerType,
        int age){
}

