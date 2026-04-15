package io.bookingmonolith.booking.listeners;

import buildingblocks.contracts.flight.FlightUpdated;
import io.bookingmonolith.booking.bookings.valueobjects.Trip;
import io.bookingmonolith.booking.data.jpa.entities.BookingEntity;
import io.bookingmonolith.booking.data.jpa.repositories.BookingRepository;
import org.slf4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FlightUpdatedListener {

    private final BookingRepository bookingRepository;
    private final Logger logger;

    public FlightUpdatedListener(BookingRepository bookingRepository, Logger logger) {
        this.bookingRepository = bookingRepository;
        this.logger = logger;
    }

    @EventListener
    public void onFlightUpdated(FlightUpdated event) {
        List<BookingEntity> bookings = bookingRepository
                .findAllByTripFlightIdAndIsDeletedFalse(event.id());

        for (BookingEntity booking : bookings) {
            Trip updatedTrip = new Trip(
                    event.id(),
                    event.flightNumber(),
                    event.aircraftId(),
                    event.departureAirportId(),
                    event.arriveAirportId(),
                    event.flightDate(),
                    event.price(),
                    booking.getTrip().getDescription(),
                    booking.getTrip().getSeatNumber()
            );
            booking.updateTrip(updatedTrip);
            bookingRepository.save(booking);
        }

        logger.info("Updated {} bookings for flight {}", bookings.size(), event.id());
    }
}
