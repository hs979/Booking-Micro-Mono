package io.bookingmonolith.passenger.passengers.features.getpassengerbyid;

import buildingblocks.mediator.abstractions.queries.IQuery;
import io.bookingmonolith.passenger.passengers.dtos.PassengerDto;

import java.util.UUID;

public record GetPassengerByIdQuery(
        UUID id
) implements IQuery<PassengerDto> {
}

