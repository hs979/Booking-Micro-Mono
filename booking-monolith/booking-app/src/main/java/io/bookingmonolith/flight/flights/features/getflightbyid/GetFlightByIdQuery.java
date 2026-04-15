package io.bookingmonolith.flight.flights.features.getflightbyid;

import buildingblocks.mediator.abstractions.queries.IQuery;
import io.bookingmonolith.flight.flights.dtos.FlightDto;
import java.util.UUID;

public record GetFlightByIdQuery(
  UUID id
) implements IQuery<FlightDto> {
}


