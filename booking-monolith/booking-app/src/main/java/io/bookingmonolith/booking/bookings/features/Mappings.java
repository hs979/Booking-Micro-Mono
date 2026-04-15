package io.bookingmonolith.booking.bookings.features;

import com.github.f4b6a3.uuid.UuidCreator;
import io.bookingmonolith.booking.bookings.dtos.BookingDto;
import io.bookingmonolith.booking.bookings.features.createbooking.CreateBookingCommand;
import io.bookingmonolith.booking.bookings.features.createbooking.CreateBookingRequestDto;
import io.bookingmonolith.booking.bookings.modles.Booking;
import io.bookingmonolith.booking.data.jpa.entities.BookingEntity;


public final class Mappings {
    public static BookingEntity toBookingEntity(Booking booking) {
        return new BookingEntity(
                booking.getId().getBookingId(),
                booking.getPassengerInfo(),
                booking.getTrip(),
                booking.getCreatedAt(),
                booking.getCreatedBy(),
                booking.getLastModified(),
                booking.getLastModifiedBy(),
                booking.getVersion(),
                booking.isDeleted()
        );
    }

    public static BookingDto toBookingDto(BookingEntity booking) {
        return new BookingDto(
                booking.getId(),
                booking.getPassengerInfo().getName(),
                booking.getTrip().getFlightNumber(),
                booking.getTrip().getAircraftId(),
                booking.getTrip().getPrice(),
                booking.getTrip().getFlightDate(),
                booking.getTrip().getSeatNumber(),
                booking.getTrip().getDepartureAirportId(),
                booking.getTrip().getArriveAirportId(),
                booking.getTrip().getDescription()
        );
    }

    public static CreateBookingCommand toCreateBookingCommand(CreateBookingRequestDto bookingRequestDto) {
        return new CreateBookingCommand(
                UuidCreator.getTimeOrderedEpoch(),
                bookingRequestDto.passengerId(),
                bookingRequestDto.flightId(),
                bookingRequestDto.description()
        );
    }
}
