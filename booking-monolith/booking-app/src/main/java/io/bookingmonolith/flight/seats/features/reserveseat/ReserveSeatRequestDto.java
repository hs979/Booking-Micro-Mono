package io.bookingmonolith.flight.seats.features.reserveseat;

import java.util.UUID;

public record ReserveSeatRequestDto(
  String seatNumber,
  UUID flightId){
}

