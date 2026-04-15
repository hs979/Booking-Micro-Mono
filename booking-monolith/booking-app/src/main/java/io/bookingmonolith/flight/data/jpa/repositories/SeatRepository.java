package io.bookingmonolith.flight.data.jpa.repositories;

import io.bookingmonolith.flight.data.jpa.entities.SeatEntity;
import io.bookingmonolith.flight.seats.features.Mappings;
import io.bookingmonolith.flight.seats.models.Seat;
import io.bookingmonolith.flight.seats.valueobjects.FlightId;
import io.bookingmonolith.flight.seats.valueobjects.SeatNumber;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;


@Repository
public interface SeatRepository extends JpaRepository<SeatEntity, UUID> {
  SeatEntity findSeatByIdAndIsDeletedFalse(UUID id);
  SeatEntity findSeatByFlightIdAndSeatNumberAndIsDeletedFalse(FlightId flightId, SeatNumber seatNumber);
  List<SeatEntity> findAllByFlightIdAndIsDeletedFalse(FlightId flightId);
}
