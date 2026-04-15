package io.bookingmonolith;

import buildingblocks.contracts.booking.BookingCreated;
import buildingblocks.contracts.flight.*;
import buildingblocks.contracts.passenger.PassengerCreated;
import buildingblocks.core.event.DomainEvent;
import buildingblocks.core.event.EventMapper;
import buildingblocks.core.event.IntegrationEvent;
import buildingblocks.core.event.InternalCommand;
import io.bookingmonolith.booking.bookings.features.createbooking.BookingCreatedDomainEvent;
import io.bookingmonolith.flight.aircrafts.features.createaircraft.AircraftCreatedDomainEvent;
import io.bookingmonolith.flight.airports.features.createairport.AirportCreatedDomainEvent;
import io.bookingmonolith.flight.flights.features.createflight.FlightCreatedDomainEvent;
import io.bookingmonolith.flight.flights.features.deleteflight.FlightDeletedDomainEvent;
import io.bookingmonolith.flight.flights.features.updateflight.FlightUpdatedDomainEvent;
import io.bookingmonolith.flight.seats.features.createseat.SeatCreatedDomainEvent;
import io.bookingmonolith.flight.seats.features.reserveseat.SeatReservedDomainEvent;
import io.bookingmonolith.passenger.passengers.features.createpassenger.PassengerCreatedDomainEvent;
import org.springframework.stereotype.Component;

@Component
public class EventMapperImpl implements EventMapper {

    @Override
    public IntegrationEvent MapToIntegrationEvent(DomainEvent event) {
        // Flight domain events
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
        // Passenger domain events
        else if (event instanceof PassengerCreatedDomainEvent e) {
            return new PassengerCreated(e.id());
        }
        // Booking domain events
        else if (event instanceof BookingCreatedDomainEvent e) {
            return new BookingCreated(e.id());
        }
        return null;
    }

    @Override
    public InternalCommand MapToInternalCommand(DomainEvent event) {
        return null;
    }
}
