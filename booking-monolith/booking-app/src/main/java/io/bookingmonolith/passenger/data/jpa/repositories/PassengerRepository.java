package io.bookingmonolith.passenger.data.jpa.repositories;

import io.bookingmonolith.passenger.data.jpa.entities.PassengerEntity;
import io.bookingmonolith.passenger.passengers.features.Mappings;
import io.bookingmonolith.passenger.passengers.models.Passenger;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;


@Repository
public interface PassengerRepository extends JpaRepository<PassengerEntity, UUID> {
    PassengerEntity findPassengerByPassportNumber_PassportNumberAndIsDeletedFalse(String passportNumber);
    PassengerEntity findPassengerByIdAndIsDeletedFalse(UUID id);
}
