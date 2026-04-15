package io.bookingmonolith.flight.flights.features.deleteflight;

import buildingblocks.mediator.abstractions.commands.ICommandHandler;
import io.bookingmonolith.flight.data.jpa.entities.FlightEntity;
import io.bookingmonolith.flight.data.jpa.repositories.FlightRepository;
import io.bookingmonolith.flight.flights.dtos.FlightDto;
import io.bookingmonolith.flight.flights.exceptions.FlightNotFoundException;
import io.bookingmonolith.flight.flights.features.Mappings;
import io.bookingmonolith.flight.flights.models.Flight;
import org.springframework.stereotype.Component;

@Component
public class DeleteFlightCommandHandler implements ICommandHandler<DeleteFlightCommand, FlightDto> {
  private final FlightRepository flightRepository;

  public DeleteFlightCommandHandler(FlightRepository flightRepository) {
    this.flightRepository = flightRepository;
  }

  @Override
  public FlightDto handle(DeleteFlightCommand command) {

    FlightEntity existingFlight = flightRepository.findFlightByIdAndIsDeletedFalse(command.id());
    if (existingFlight == null) {
      throw new FlightNotFoundException();
    }

    Flight flight = Mappings.toFlightAggregate(existingFlight);

    flight.delete();

    FlightEntity flightEntity = Mappings.toFlightEntity(flight);

    FlightEntity updatedFlight = flightRepository.save(flightEntity);
    return Mappings.toFlightDto(updatedFlight);
  }
}
