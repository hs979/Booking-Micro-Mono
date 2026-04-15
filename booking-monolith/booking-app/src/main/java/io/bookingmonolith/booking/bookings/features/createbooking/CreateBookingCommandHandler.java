package io.bookingmonolith.booking.bookings.features.createbooking;

import buildingblocks.mediator.abstractions.IMediator;
import buildingblocks.mediator.abstractions.commands.ICommandHandler;
import io.bookingmonolith.booking.bookings.dtos.BookingDto;
import io.bookingmonolith.booking.bookings.exceptions.BookingAlreadyExistException;
import io.bookingmonolith.booking.bookings.exceptions.FlightNotFoundException;
import io.bookingmonolith.booking.bookings.exceptions.PassengerNotFoundException;
import io.bookingmonolith.booking.bookings.exceptions.SeatNumberIsNotAvailableException;
import io.bookingmonolith.booking.bookings.features.Mappings;
import io.bookingmonolith.booking.bookings.modles.Booking;
import io.bookingmonolith.booking.bookings.valueobjects.BookingId;
import io.bookingmonolith.booking.bookings.valueobjects.PassengerInfo;
import io.bookingmonolith.booking.bookings.valueobjects.Trip;
import io.bookingmonolith.booking.data.jpa.entities.BookingEntity;
import io.bookingmonolith.booking.data.jpa.repositories.BookingRepository;
import io.bookingmonolith.flight.flights.dtos.FlightDto;
import io.bookingmonolith.flight.flights.features.getflightbyid.GetFlightByIdQuery;
import io.bookingmonolith.flight.seats.dtos.SeatDto;
import io.bookingmonolith.flight.seats.features.getavailableseats.GetAvailableSeatsQuery;
import io.bookingmonolith.flight.seats.features.reserveseat.ReserveSeatCommand;
import io.bookingmonolith.passenger.passengers.dtos.PassengerDto;
import io.bookingmonolith.passenger.passengers.features.getpassengerbyid.GetPassengerByIdQuery;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import static io.bookingmonolith.booking.bookings.features.Mappings.toBookingDto;

@Service
public class CreateBookingCommandHandler implements ICommandHandler<CreateBookingCommand, BookingDto> {

    private final IMediator mediator;
    private final BookingRepository bookingRepository;

    public CreateBookingCommandHandler(
            IMediator mediator,
            BookingRepository bookingRepository) {
        this.mediator = mediator;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public BookingDto handle(CreateBookingCommand command) {

        FlightDto flight = mediator.send(new GetFlightByIdQuery(command.flightId()));

        if (flight == null) {
            throw new FlightNotFoundException();
        }

        PassengerDto passenger = mediator.send(new GetPassengerByIdQuery(command.passengerId()));

        if (passenger == null) {
            throw new PassengerNotFoundException();
        }

        List<SeatDto> availableSeats = mediator.send(new GetAvailableSeatsQuery(command.flightId()));
        SeatDto emptySeat = availableSeats.stream().findAny().orElse(null);

        if (emptySeat == null) {
            throw new SeatNumberIsNotAvailableException();
        }

        var existBooking = bookingRepository.findBookingByIdAndIsDeletedFalse(command.flightId());

        if (existBooking != null) {
            throw new BookingAlreadyExistException();
        }

        Booking booking = Booking.create(new BookingId(command.id()), new PassengerInfo(passenger.name()), new Trip(
                flight.id(),
                flight.flightNumber(),
                flight.aircraftId(),
                flight.departureAirportId(),
                flight.arriveAirportId(),
                flight.flightDate(),
                BigDecimal.ONE,
                command.description(),
                emptySeat.seatNumber()));

        mediator.send(new ReserveSeatCommand(emptySeat.seatNumber(), command.flightId()));

        BookingEntity bookingEntity = Mappings.toBookingEntity(booking);
        BookingEntity createdBooking = bookingRepository.save(bookingEntity);

        return toBookingDto(createdBooking);
    }
}
