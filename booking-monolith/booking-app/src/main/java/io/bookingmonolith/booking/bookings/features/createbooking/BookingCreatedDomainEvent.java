package io.bookingmonolith.booking.bookings.features.createbooking;

import buildingblocks.core.event.DomainEvent;
import io.bookingmonolith.booking.bookings.valueobjects.PassengerInfo;
import io.bookingmonolith.booking.bookings.valueobjects.Trip;
import java.util.UUID;


public record BookingCreatedDomainEvent(
        UUID id,
        PassengerInfo passengerInfo,
        Trip trip,
        boolean isDeleted) implements DomainEvent {
}
