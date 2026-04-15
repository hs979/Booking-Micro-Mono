package io.bookingmonolith.flight.aircrafts.features.createaircraft;

public record CreateAircraftRequestDto(
  String name,
  String model,
  int manufacturingYear){
}
