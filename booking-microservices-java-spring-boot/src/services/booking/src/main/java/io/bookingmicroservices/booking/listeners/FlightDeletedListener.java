package io.bookingmicroservices.booking.listeners;

import buildingblocks.contracts.flight.FlightDeleted;
import buildingblocks.rabbitmq.MessageHandler;
import io.bookingmicroservices.booking.data.jpa.entities.BookingEntity;
import io.bookingmicroservices.booking.data.jpa.repositories.BookingRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FlightDeletedListener implements MessageHandler<FlightDeleted> {

    private final BookingRepository bookingRepository;
    private final Logger logger;

    public FlightDeletedListener(BookingRepository bookingRepository, Logger logger) {
        this.bookingRepository = bookingRepository;
        this.logger = logger;
    }

    @Override
    public void onMessage(FlightDeleted event) {
        List<BookingEntity> bookings = bookingRepository
                .findAllByTripFlightIdAndIsDeletedFalse(event.Id());

        for (BookingEntity booking : bookings) {
            booking.markAsDeleted();
            bookingRepository.save(booking);
        }

        logger.info("Cancelled {} bookings for deleted flight {}", bookings.size(), event.Id());
    }
}
