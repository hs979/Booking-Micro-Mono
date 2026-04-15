package io.bookingmonolith.flight.flights.features.updateflight;

import buildingblocks.mediator.abstractions.commands.ICommandHandler;
import io.bookingmonolith.flight.aircrafts.valueobjects.AircraftId;
import io.bookingmonolith.flight.airports.valueobjects.AirportId;
import io.bookingmonolith.flight.data.jpa.entities.FlightEntity;
import io.bookingmonolith.flight.data.jpa.repositories.FlightRepository;
import io.bookingmonolith.flight.flights.dtos.FlightDto;
import io.bookingmonolith.flight.flights.exceptions.FlightNotFoundException;
import io.bookingmonolith.flight.flights.features.Mappings;
import io.bookingmonolith.flight.flights.models.Flight;
import io.bookingmonolith.flight.flights.valueobjects.*;
import org.springframework.stereotype.Service;

@Service
public class UpdateFlightCommandHandler implements ICommandHandler<UpdateFlightCommand, FlightDto> {
  private final FlightRepository flightRepository;

  public UpdateFlightCommandHandler(FlightRepository flightRepository) {
    this.flightRepository = flightRepository;
  }

  @Override
  public FlightDto handle(UpdateFlightCommand command) {

    FlightEntity existingFlight = flightRepository.findFlightByIdAndIsDeletedFalse(command.id());
    if (existingFlight == null) {
      throw new FlightNotFoundException();
    }

    Flight flight = Mappings.toFlightAggregate(existingFlight);

    flight.update(new FlightId(existingFlight.getId()), new FlightNumber(command.flightNumber()), new AircraftId(command.aircraftId()), new AirportId(command.departureAirportId()), new DepartureDate(command.departureDate()),
      new ArriveDate(command.arriveDate()), new AirportId(command.arriveAirportId()), new DurationMinutes(command.durationMinutes()), new FlightDate(command.flightDate()),
      command.status(), new Price(command.price()), command.isDeleted());

    FlightEntity flightEntity = Mappings.toFlightEntity(flight);

    FlightEntity updatedFlight = flightRepository.save(flightEntity);
    return Mappings.toFlightDto(updatedFlight);
  }
}
