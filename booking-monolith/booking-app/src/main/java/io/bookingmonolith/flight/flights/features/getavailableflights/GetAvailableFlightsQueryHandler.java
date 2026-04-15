package io.bookingmonolith.flight.flights.features.getavailableflights;

import buildingblocks.mediator.abstractions.queries.IQueryHandler;
import io.bookingmonolith.flight.data.jpa.entities.FlightEntity;
import io.bookingmonolith.flight.data.jpa.repositories.FlightRepository;
import io.bookingmonolith.flight.flights.dtos.FlightDto;
import io.bookingmonolith.flight.flights.features.Mappings;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAvailableFlightsQueryHandler implements IQueryHandler<GetAvailableFlightsQuery, List<FlightDto>> {
  private final FlightRepository flightRepository;

  public GetAvailableFlightsQueryHandler(FlightRepository flightRepository) {
    this.flightRepository = flightRepository;
  }

  @Override
  public List<FlightDto> handle(GetAvailableFlightsQuery query) {
    List<FlightEntity> flightEntities = flightRepository.findAllByIsDeletedFalse();
    return flightEntities.stream().map(Mappings::toFlightDto).toList();
  }
}
