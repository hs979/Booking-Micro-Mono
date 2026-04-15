package io.bookingmonolith.flight.flights.features.getflightbyid;

import buildingblocks.mediator.abstractions.queries.IQueryHandler;
import io.bookingmonolith.flight.data.jpa.entities.FlightEntity;
import io.bookingmonolith.flight.data.jpa.repositories.FlightRepository;
import io.bookingmonolith.flight.flights.dtos.FlightDto;
import io.bookingmonolith.flight.flights.exceptions.FlightNotFoundException;
import io.bookingmonolith.flight.flights.features.Mappings;
import org.springframework.stereotype.Service;

@Service
public class GetFlightByIdQueryHandler implements IQueryHandler<GetFlightByIdQuery, FlightDto> {
  private final FlightRepository flightRepository;

  public GetFlightByIdQueryHandler(FlightRepository flightRepository) {
    this.flightRepository = flightRepository;
  }

  @Override
  public FlightDto handle(GetFlightByIdQuery query) {
    FlightEntity flightEntity = flightRepository.findFlightByIdAndIsDeletedFalse(query.id());

    if (flightEntity == null) {
      throw new FlightNotFoundException();
    }

    return Mappings.toFlightDto(flightEntity);
  }
}
