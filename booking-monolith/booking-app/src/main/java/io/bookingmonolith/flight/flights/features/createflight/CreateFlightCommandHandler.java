package io.bookingmonolith.flight.flights.features.createflight;

import buildingblocks.mediator.abstractions.commands.ICommandHandler;
import io.bookingmonolith.flight.aircrafts.valueobjects.AircraftId;
import io.bookingmonolith.flight.airports.valueobjects.AirportId;
import io.bookingmonolith.flight.data.jpa.entities.FlightEntity;
import io.bookingmonolith.flight.data.jpa.repositories.FlightRepository;
import io.bookingmonolith.flight.flights.dtos.FlightDto;
import io.bookingmonolith.flight.flights.exceptions.FlightAlreadyExistException;
import io.bookingmonolith.flight.flights.features.Mappings;
import io.bookingmonolith.flight.flights.models.Flight;
import io.bookingmonolith.flight.flights.valueobjects.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateFlightCommandHandler implements ICommandHandler<CreateFlightCommand, FlightDto> {
  private final FlightRepository flightRepository;

  public CreateFlightCommandHandler(
    FlightRepository flightRepository) {
    this.flightRepository = flightRepository;
  }

  @Override
  public FlightDto handle(CreateFlightCommand command) {

    FlightEntity existFlight = flightRepository.findFlightByIdAndIsDeletedFalse(command.id());
    if (existFlight!= null) {
      throw new FlightAlreadyExistException();
    }

    Flight flight = Flight.create(
      new FlightId(command.id()),
      new FlightNumber(command.flightNumber()),
      new AircraftId(command.aircraftId()),
      new AirportId(command.departureAirportId()),
      new DepartureDate(command.departureDate()),
      new ArriveDate(command.arriveDate()),
      new AirportId(command.arriveAirportId()),
      new DurationMinutes(command.durationMinutes()),
      new FlightDate(command.flightDate()),
      command.status(),
      new Price(command.price())
    );

    FlightEntity flightEntity = Mappings.toFlightEntity(flight);

    FlightEntity flightCreated = flightRepository.save(flightEntity);
    return Mappings.toFlightDto(flightCreated);
  }
}
