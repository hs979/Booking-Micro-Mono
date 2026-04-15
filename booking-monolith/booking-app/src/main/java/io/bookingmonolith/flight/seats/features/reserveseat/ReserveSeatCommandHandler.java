package io.bookingmonolith.flight.seats.features.reserveseat;

import buildingblocks.mediator.abstractions.commands.ICommandHandler;
import io.bookingmonolith.flight.data.jpa.entities.SeatEntity;
import io.bookingmonolith.flight.data.jpa.repositories.SeatRepository;
import io.bookingmonolith.flight.seats.dtos.SeatDto;
import io.bookingmonolith.flight.seats.exceptions.SeatNumberAlreadyReservedException;
import io.bookingmonolith.flight.seats.features.Mappings;
import io.bookingmonolith.flight.seats.models.Seat;
import io.bookingmonolith.flight.seats.valueobjects.FlightId;
import io.bookingmonolith.flight.seats.valueobjects.SeatNumber;
import org.springframework.stereotype.Service;


@Service
public class ReserveSeatCommandHandler implements ICommandHandler<ReserveSeatCommand, SeatDto> {
  private final SeatRepository seatRepository;

  public ReserveSeatCommandHandler(SeatRepository seatRepository) {
    this.seatRepository = seatRepository;
  }

  @Override
  public SeatDto handle(ReserveSeatCommand command) {
    SeatEntity existSeat = seatRepository.findSeatByFlightIdAndSeatNumberAndIsDeletedFalse(new FlightId(command.flightId()), new SeatNumber(command.seatNumber()));

    if (existSeat == null) {
         throw new SeatNumberAlreadyReservedException();
    }

    Seat seat = Mappings.toSeatAggregate(existSeat);

    seat.reserveSeat();

    SeatEntity seatEntity = Mappings.toSeatEntity(seat);
    SeatEntity seatUpdated = seatRepository.save(seatEntity);

    return Mappings.toSeatDto(seatUpdated);
  }
}
