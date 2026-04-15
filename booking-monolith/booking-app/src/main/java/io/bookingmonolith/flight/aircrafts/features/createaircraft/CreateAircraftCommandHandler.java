package io.bookingmonolith.flight.aircrafts.features.createaircraft;

import buildingblocks.mediator.abstractions.commands.ICommandHandler;
import io.bookingmonolith.flight.aircrafts.dtos.AircraftDto;
import io.bookingmonolith.flight.aircrafts.exceptions.AircraftAlreadyExistException;
import io.bookingmonolith.flight.aircrafts.features.Mappings;
import io.bookingmonolith.flight.aircrafts.models.Aircraft;
import io.bookingmonolith.flight.aircrafts.valueobjects.AircraftId;
import io.bookingmonolith.flight.aircrafts.valueobjects.ManufacturingYear;
import io.bookingmonolith.flight.aircrafts.valueobjects.Model;
import io.bookingmonolith.flight.aircrafts.valueobjects.Name;
import io.bookingmonolith.flight.data.jpa.entities.AircraftEntity;
import io.bookingmonolith.flight.data.jpa.repositories.AircraftRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateAircraftCommandHandler implements ICommandHandler<CreateAircraftCommand, AircraftDto> {

  private final AircraftRepository aircraftRepository;

  public CreateAircraftCommandHandler(AircraftRepository aircraftRepository) {
    this.aircraftRepository = aircraftRepository;
  }

  @Override
  public AircraftDto handle(CreateAircraftCommand command) {

    AircraftEntity existAircraft = aircraftRepository.findAircraftByModel_ModelAndIsDeletedFalse(command.model());
    if (existAircraft != null) {
      throw new AircraftAlreadyExistException();
    }

    Aircraft aircraft = Aircraft.create(
      new AircraftId(command.id()),
      new Name(command.name()),
      new Model(command.model()),
      new ManufacturingYear(command.manufacturingYear())
    );

    AircraftEntity aircraftEntity = Mappings.toAircraftEntity(aircraft);

    AircraftEntity aircraftCreated = aircraftRepository.save(aircraftEntity);
    return Mappings.toAircraftDto(aircraftCreated);
  }
}
