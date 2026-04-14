package io.bookingmicroservices.flight.flights.features.getavailableflights;

import buildingblocks.mediator.abstractions.queries.IQueryHandler;
import io.bookingmicroservices.flight.data.jpa.entities.FlightEntity;
import io.bookingmicroservices.flight.data.jpa.repositories.FlightRepository;
import io.bookingmicroservices.flight.flights.dtos.FlightDto;
import io.bookingmicroservices.flight.flights.features.Mappings;
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
