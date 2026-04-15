package io.bookingmonolith.flight.data.jpa.repositories;

import io.bookingmonolith.flight.aircrafts.features.Mappings;
import io.bookingmonolith.flight.aircrafts.models.Aircraft;
import io.bookingmonolith.flight.data.jpa.entities.AircraftEntity;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AircraftRepository extends JpaRepository<AircraftEntity, UUID> {
  AircraftEntity findAircraftByModel_ModelAndIsDeletedFalse(String model);
}
