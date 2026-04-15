package io.bookingmonolith.booking.listeners;

import buildingblocks.contracts.flight.FlightDeleted;
import io.bookingmonolith.booking.data.jpa.entities.BookingEntity;
import io.bookingmonolith.booking.data.jpa.repositories.BookingRepository;
import org.slf4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FlightDeletedListener {

    private final BookingRepository bookingRepository;
    private final Logger logger;

    public FlightDeletedListener(BookingRepository bookingRepository, Logger logger) {
        this.bookingRepository = bookingRepository;
        this.logger = logger;
    }

    @EventListener
    public void onFlightDeleted(FlightDeleted event) {
        List<BookingEntity> bookings = bookingRepository
                .findAllByTripFlightIdAndIsDeletedFalse(event.Id());

        for (BookingEntity booking : bookings) {
            booking.markAsDeleted();
            bookingRepository.save(booking);
        }

        logger.info("Cancelled {} bookings for deleted flight {}", bookings.size(), event.Id());
    }
}
