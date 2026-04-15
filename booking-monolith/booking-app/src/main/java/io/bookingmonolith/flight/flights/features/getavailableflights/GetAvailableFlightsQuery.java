package io.bookingmonolith.flight.flights.features.getavailableflights;

import buildingblocks.mediator.abstractions.queries.IQuery;
import io.bookingmonolith.flight.flights.dtos.FlightDto;
import java.util.List;

public record GetAvailableFlightsQuery() implements IQuery<List<FlightDto>> {
}


