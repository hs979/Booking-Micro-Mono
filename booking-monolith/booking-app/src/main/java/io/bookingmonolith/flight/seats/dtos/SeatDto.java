package io.bookingmonolith.flight.seats.dtos;

import io.bookingmonolith.flight.seats.enums.SeatClass;
import io.bookingmonolith.flight.seats.enums.SeatType;
import java.util.UUID;

public record SeatDto(
  UUID id,
  String seatNumber,
  SeatType seatType,
  SeatClass seatClass,
  UUID flightId
) { }
