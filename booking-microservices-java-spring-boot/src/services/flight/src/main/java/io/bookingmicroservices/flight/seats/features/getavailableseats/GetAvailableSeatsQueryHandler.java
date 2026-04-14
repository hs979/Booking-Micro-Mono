package io.bookingmicroservices.flight.seats.features.getavailableseats;

import buildingblocks.mediator.abstractions.queries.IQueryHandler;
import io.bookingmicroservices.flight.data.jpa.entities.SeatEntity;
import io.bookingmicroservices.flight.data.jpa.repositories.SeatRepository;
import io.bookingmicroservices.flight.seats.dtos.SeatDto;
import io.bookingmicroservices.flight.seats.features.Mappings;
import io.bookingmicroservices.flight.seats.valueobjects.FlightId;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class GetAvailableSeatsQueryHandler implements IQueryHandler<GetAvailableSeatsQuery, List<SeatDto>> {
  private final SeatRepository seatRepository;

  public GetAvailableSeatsQueryHandler(SeatRepository seatRepository) {
    this.seatRepository = seatRepository;
  }

  @Override
  public List<SeatDto> handle(GetAvailableSeatsQuery query) {
    List<SeatEntity> seats = seatRepository.findAllByFlightIdAndIsDeletedFalse(new FlightId(query.flightId()));
    return seats.stream().map(Mappings::toSeatDto).toList();
  }
}

