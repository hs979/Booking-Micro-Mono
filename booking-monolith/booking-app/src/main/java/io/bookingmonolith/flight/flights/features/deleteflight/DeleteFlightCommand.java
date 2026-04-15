package io.bookingmonolith.flight.flights.features.deleteflight;

import buildingblocks.core.event.InternalCommand;
import buildingblocks.mediator.abstractions.commands.ICommand;
import io.bookingmonolith.flight.flights.dtos.FlightDto;
import java.util.UUID;

public record DeleteFlightCommand(
  UUID id
) implements ICommand<FlightDto>, InternalCommand {
}

