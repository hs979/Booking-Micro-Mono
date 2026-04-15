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
      if (event instanceof FlightCreatedDomainEvent e) {
        return new FlightCreated(e.id());
      } else if (event instanceof FlightUpdatedDomainEvent e) {
        return new FlightUpdated(e.id(), e.flightNumber(), e.aircraftId(), e.departureAirportId(), e.arriveAirportId(), e.flightDate(), e.price());
      } else if (event instanceof FlightDeletedDomainEvent e) {
        return new FlightDeleted(e.id());
      } else if (event instanceof AirportCreatedDomainEvent e) {
        return new AirportCreated(e.id());
      } else if (event instanceof AircraftCreatedDomainEvent e) {
        return new AircraftCreated(e.id());
      } else if (event instanceof SeatCreatedDomainEvent e) {
        return new SeatCreated(e.id());
      } else if (event instanceof SeatReservedDomainEvent e) {
        return new SeatReserved(e.id());
      }
      return null;
  }

  @Override
  public InternalCommand MapToInternalCommand(DomainEvent event) {
    return null;
  }
}
