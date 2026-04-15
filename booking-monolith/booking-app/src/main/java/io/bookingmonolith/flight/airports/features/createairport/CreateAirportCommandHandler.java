package io.bookingmonolith.flight.airports.features.createairport;

import buildingblocks.mediator.abstractions.commands.ICommandHandler;
import io.bookingmonolith.flight.airports.dtos.AirportDto;
import io.bookingmonolith.flight.airports.exceptions.AirportAlreadyExistException;
import io.bookingmonolith.flight.airports.features.Mappings;
import io.bookingmonolith.flight.airports.models.Airport;
import io.bookingmonolith.flight.airports.valueobjects.Address;
import io.bookingmonolith.flight.airports.valueobjects.AirportId;
import io.bookingmonolith.flight.airports.valueobjects.Code;
import io.bookingmonolith.flight.airports.valueobjects.Name;
import io.bookingmonolith.flight.data.jpa.entities.AirportEntity;
import io.bookingmonolith.flight.data.jpa.repositories.AirportRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateAirportCommandHandler implements ICommandHandler<CreateAirportCommand, AirportDto> {
  private final AirportRepository airportRepository;

  public CreateAirportCommandHandler(
    AirportRepository airportRepository) {
    this.airportRepository = airportRepository;
  }

  @Override
  public AirportDto handle(CreateAirportCommand command) {

    AirportEntity existAirport = airportRepository.findAirportByCode_CodeAndIsDeletedFalse(command.code());
    if (existAirport != null) {
      throw new AirportAlreadyExistException();
    }

    Airport airport = Airport.create(
      new AirportId(command.id()),
      new Name(command.name()),
      new Code(command.code()),
      new Address(command.address())
    );

    AirportEntity airportEntity = Mappings.toAirportEntity(airport);

    AirportEntity airportCreated = airportRepository.save(airportEntity);
    return Mappings.toAirportDto(airportCreated);
  }
}
