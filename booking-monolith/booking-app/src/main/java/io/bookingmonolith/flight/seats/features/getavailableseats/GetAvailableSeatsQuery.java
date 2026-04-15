package io.bookingmonolith.flight.seats.features.getavailableseats;

import buildingblocks.mediator.abstractions.queries.IQuery;
import io.bookingmonolith.flight.seats.dtos.SeatDto;
import java.util.List;
import java.util.UUID;


public record GetAvailableSeatsQuery(
  UUID flightId
) implements IQuery<List<SeatDto>> {
}


