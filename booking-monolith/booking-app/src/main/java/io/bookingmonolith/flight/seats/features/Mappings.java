package io.bookingmonolith.flight.seats.features;

import com.github.f4b6a3.uuid.UuidCreator;
import io.bookingmonolith.flight.data.jpa.entities.SeatEntity;
import io.bookingmonolith.flight.seats.dtos.SeatDto;
import io.bookingmonolith.flight.seats.features.createseat.CreateSeatCommand;
import io.bookingmonolith.flight.seats.features.createseat.CreateSeatRequestDto;
import io.bookingmonolith.flight.seats.features.reserveseat.ReserveSeatCommand;
import io.bookingmonolith.flight.seats.features.reserveseat.ReserveSeatRequestDto;
import io.bookingmonolith.flight.seats.models.Seat;
import io.bookingmonolith.flight.seats.valueobjects.SeatId;

public final class Mappings {

  public static SeatEntity toSeatEntity(Seat seat) {
    return new SeatEntity(
      seat.getId().getSeatId(),
      seat.getSeatNumber(),
      seat.getSeatType(),
      seat.getSeatClass(),
      seat.getFlightId(),
      seat.getCreatedAt(),
      seat.getCreatedBy(),
      seat.getLastModified(),
      seat.getLastModifiedBy(),
      seat.getVersion(),
      seat.isDeleted()
    );
  }

  public static Seat toSeatAggregate(SeatEntity seatEntity) {
    return new Seat(
      new SeatId(seatEntity.getId()),
      seatEntity.getSeatNumber(),
      seatEntity.getType(),
      seatEntity.getSeatClass(),
      seatEntity.getFlightId(),
      seatEntity.getCreatedAt(),
      seatEntity.getCreatedBy(),
      seatEntity.getLastModified(),
      seatEntity.getLastModifiedBy(),
      seatEntity.getVersion(),
      seatEntity.isDeleted()
    );
  }

  public static SeatDto toSeatDto(SeatEntity seatEntity) {
    return new SeatDto(
      seatEntity.getId(),
      seatEntity.getSeatNumber().getSeatNumber(),
      seatEntity.getType(),
      seatEntity.getSeatClass(),
      seatEntity.getFlightId().getFlightId());
  }

  public static CreateSeatCommand toCreateSeatCommand(CreateSeatRequestDto createSeatRequestDto) {
    return new CreateSeatCommand(
      UuidCreator.getTimeOrderedEpoch(),
      createSeatRequestDto.seatNumber(),
      createSeatRequestDto.seatType(),
      createSeatRequestDto.seatClass(),
      createSeatRequestDto.flightId()
    );
  }


  public static ReserveSeatCommand toReserveSeatCommand(ReserveSeatRequestDto reserveSeatRequestDto) {
    return new ReserveSeatCommand(
      reserveSeatRequestDto.seatNumber(),
      reserveSeatRequestDto.flightId()
    );
  }

}
