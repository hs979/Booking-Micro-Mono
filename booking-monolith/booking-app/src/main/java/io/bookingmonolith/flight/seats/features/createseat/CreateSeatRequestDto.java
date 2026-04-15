package io.bookingmonolith.flight.seats.features.createseat;

import io.bookingmonolith.flight.seats.enums.SeatClass;
import io.bookingmonolith.flight.seats.enums.SeatType;
import java.util.UUID;

public record CreateSeatRequestDto(
  String seatNumber,
  SeatType seatType,
  SeatClass seatClass,
  UUID flightId){
}

