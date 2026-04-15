package io.bookingmonolith.flight.flights.features;

import com.github.f4b6a3.uuid.UuidCreator;
import io.bookingmonolith.flight.aircrafts.valueobjects.AircraftId;
import io.bookingmonolith.flight.airports.valueobjects.AirportId;
import io.bookingmonolith.flight.data.jpa.entities.FlightEntity;
import io.bookingmonolith.flight.flights.dtos.FlightDto;
import io.bookingmonolith.flight.flights.features.createflight.CreateFlightCommand;
import io.bookingmonolith.flight.flights.features.createflight.CreateFlightRequestDto;
import io.bookingmonolith.flight.flights.features.updateflight.UpdateFlightCommand;
import io.bookingmonolith.flight.flights.features.updateflight.UpdateFlightRequestDto;
import io.bookingmonolith.flight.flights.models.Flight;
import io.bookingmonolith.flight.flights.valueobjects.*;

import java.util.UUID;

public final class Mappings {

  public static FlightEntity toFlightEntity(Flight flight) {
    return new FlightEntity(
      flight.getId().getFlightId(),
      flight.getFlightNumber(),
      flight.getAircraftId(),
      flight.getDepartureAirportId(),
      flight.getArriveAirportId(),
      flight.getDurationMinutes(),
      flight.getStatus(),
      flight.getPrice(),
      flight.getArriveDate(),
      flight.getDepartureDate(),
      flight.getFlightDate(),
      flight.getCreatedAt(),
      flight.getCreatedBy(),
      flight.getLastModified(),
      flight.getLastModifiedBy(),
      flight.getVersion(),
      flight.isDeleted()
    );
  }

  public static Flight toFlightAggregate(FlightEntity flightEntity) {
    return new Flight(
      new FlightId(flightEntity.getId()),
      flightEntity.getFlightNumber(),
      flightEntity.getAircraftId(),
      flightEntity.getArriveAirportId(),
      flightEntity.getDepartureAirportId(),
      flightEntity.getDurationMinutes(),
      flightEntity.getStatus(),
      flightEntity.getPrice(),
      flightEntity.getArriveDate(),
      flightEntity.getDepartureDate(),
      flightEntity.getFlightDate(),
      flightEntity.getCreatedAt(),
      flightEntity.getCreatedBy(),
      flightEntity.getLastModified(),
      flightEntity.getLastModifiedBy(),
      flightEntity.getVersion(),
      flightEntity.isDeleted()
    );
  }

  public static FlightEntity toFlightEntity(CreateFlightCommand createFlightCommand) {
    return new FlightEntity(
      createFlightCommand.id(),
      new FlightNumber(createFlightCommand.flightNumber()),
      new AircraftId(createFlightCommand.aircraftId()),
      new AirportId(createFlightCommand.departureAirportId()),
      new AirportId(createFlightCommand.arriveAirportId()),
      new DurationMinutes(createFlightCommand.durationMinutes()),
      createFlightCommand.status(),
      new Price(createFlightCommand.price()),
      new ArriveDate(createFlightCommand.arriveDate()),
      new DepartureDate(createFlightCommand.departureDate()),
      new FlightDate(createFlightCommand.flightDate())
    );
  }

  public static CreateFlightCommand toCreateFlightCommand(CreateFlightRequestDto createFlightRequestDto) {
    return new CreateFlightCommand(
      UuidCreator.getTimeOrderedEpoch(),
      createFlightRequestDto.flightNumber(),
      createFlightRequestDto.aircraftId(),
      createFlightRequestDto.departureAirportId(),
      createFlightRequestDto.departureDate(),
      createFlightRequestDto.arriveDate(),
      createFlightRequestDto.arriveAirportId(),
      createFlightRequestDto.durationMinutes(),
      createFlightRequestDto.flightDate(),
      createFlightRequestDto.status(),
      createFlightRequestDto.price()
    );
  }

  public static UpdateFlightCommand toUpdateFlightCommand(UUID id, UpdateFlightRequestDto updateFlightRequestDto) {
    return new UpdateFlightCommand(
      id,
      updateFlightRequestDto.flightNumber(),
      updateFlightRequestDto.aircraftId(),
      updateFlightRequestDto.departureAirportId(),
      updateFlightRequestDto.departureDate(),
      updateFlightRequestDto.arriveDate(),
      updateFlightRequestDto.arriveAirportId(),
      updateFlightRequestDto.durationMinutes(),
      updateFlightRequestDto.flightDate(),
      updateFlightRequestDto.status(),
      updateFlightRequestDto.price(),
      updateFlightRequestDto.isDeleted()
    );
  }

  public static FlightDto toFlightDto(FlightEntity flightEntity) {
    return new FlightDto(
      flightEntity.getId(),
      flightEntity.getFlightNumber().getFlightNumber(),
      flightEntity.getAircraftId().getAircraftId(),
      flightEntity.getDepartureAirportId().getAirportId(),
      flightEntity.getDepartureDate().getDepartureDate(),
      flightEntity.getArriveDate().getArriveDate(),
      flightEntity.getArriveAirportId().getAirportId(),
      flightEntity.getDurationMinutes().getDurationMinutes(),
      flightEntity.getFlightDate().getFlightDate(),
      flightEntity.getStatus(),
      flightEntity.getPrice().getPrice());
  }


}
