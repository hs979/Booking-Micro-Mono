package io.bookingmonolith.flight.seats.features.createseat;

import buildingblocks.core.event.DomainEvent;
import io.bookingmonolith.flight.seats.enums.SeatClass;
import io.bookingmonolith.flight.seats.enums.SeatType;
import java.util.UUID;


public record SeatCreatedDomainEvent(
  UUID id,
  String seatNumber,
  SeatType seatType,
  SeatClass seatClass,
  UUID flightId,
  boolean isDeleted) implements DomainEvent {
}

