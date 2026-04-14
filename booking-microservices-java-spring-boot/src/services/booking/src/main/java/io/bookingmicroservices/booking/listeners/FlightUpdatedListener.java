package io.bookingmicroservices.booking.listeners;

import buildingblocks.contracts.flight.FlightUpdated;
import buildingblocks.rabbitmq.MessageHandler;
import io.bookingmicroservices.booking.bookings.valueobjects.Trip;
import io.bookingmicroservices.booking.data.jpa.entities.BookingEntity;
import io.bookingmicroservices.booking.data.jpa.repositories.BookingRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FlightUpdatedListener implements MessageHandler<FlightUpdated> {

    private final BookingRepository bookingRepository;
    private final Logger logger;

    public FlightUpdatedListener(BookingRepository bookingRepository, Logger logger) {
        this.bookingRepository = bookingRepository;
        this.logger = logger;
    }

    @Override
    public void onMessage(FlightUpdated event) {
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
