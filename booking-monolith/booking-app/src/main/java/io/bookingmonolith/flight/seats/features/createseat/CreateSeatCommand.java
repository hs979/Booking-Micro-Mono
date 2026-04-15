package io.bookingmonolith.flight.seats.features.createseat;

import buildingblocks.core.event.InternalCommand;
import buildingblocks.mediator.abstractions.commands.ICommand;
import com.github.f4b6a3.uuid.UuidCreator;
import io.bookingmonolith.flight.seats.dtos.SeatDto;
import io.bookingmonolith.flight.seats.enums.SeatClass;
import io.bookingmonolith.flight.seats.enums.SeatType;

import java.util.UUID;

public record CreateSeatCommand(
  UUID id,
  String seatNumber,
  SeatType seatType,
  SeatClass seatClass,
  UUID flightId
) implements ICommand<SeatDto>, InternalCommand {
}


