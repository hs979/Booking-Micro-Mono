package io.bookingmicroservices.flight;

import buildingblocks.contracts.flight.*;
import buildingblocks.core.event.EventMapper;
import buildingblocks.core.event.DomainEvent;
import buildingblocks.core.event.IntegrationEvent;
import buildingblocks.core.event.InternalCommand;
import io.bookingmicroservices.flight.aircrafts.features.createaircraft.AircraftCreatedDomainEvent;
import io.bookingmicroservices.flight.airports.features.createairport.AirportCreatedDomainEvent;
import io.bookingmicroservices.flight.flights.features.createflight.FlightCreatedDomainEvent;
import io.bookingmicroservices.flight.flights.features.deleteflight.FlightDeletedDomainEvent;
import io.bookingmicroservices.flight.flights.features.updateflight.FlightUpdatedDomainEvent;
import io.bookingmicroservices.flight.seats.features.createseat.SeatCreatedDomainEvent;
import io.bookingmicroservices.flight.seats.features.reserveseat.SeatReservedDomainEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class EventMapperImpl implements EventMapper {
  @Override
  public IntegrationEvent MapToIntegrationEvent(@NotNull DomainEvent event) {
      return switch (event) {
        case FlightCreatedDomainEvent e -> new FlightCreated(e.id());
        case FlightUpdatedDomainEvent e -> new FlightUpdated(e.id(), e.flightNumber(), e.aircraftId(), e.departureAirportId(), e.arriveAirportId(), e.flightDate(), e.price());
        case FlightDeletedDomainEvent e -> new FlightDeleted(e.id());
        case AirportCreatedDomainEvent e -> new AirportCreated(e.id());
        case AircraftCreatedDomainEvent e -> new AircraftCreated(e.id());
        case SeatCreatedDomainEvent e -> new SeatCreated(e.id());
        case SeatReservedDomainEvent e -> new SeatReserved(e.id());
        default -> null;
      };
  }

  @Override
  public InternalCommand MapToInternalCommand(DomainEvent event) {
    return null;
  }
}
