package io.bookingmicroservices.flight.data.jpa.seeds;

import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class FlightDataSeeder implements CommandLineRunner {

  private final EntityManager entityManager;
  private final TransactionTemplate transactionTemplate;
  private final Logger logger;

  public FlightDataSeeder(
    EntityManager entityManager,
    PlatformTransactionManager platformTransactionManager,
    Logger logger) {
    this.entityManager = entityManager;
    this.transactionTemplate = new TransactionTemplate(platformTransactionManager);
    this.logger = logger;
  }

  @Override
  public void run(String... args) throws Exception {
    transactionTemplate.execute(status -> {
      try {
        logger.info("Data seeder is started.");

        seedAirport();
        seedAircraft();
        seedFlight();
        seedSeat();

        logger.info("Data seeder is finished.");

        return null;
      }catch (Exception ex) {
        status.setRollbackOnly();
        logger.error(ex.getMessage(), ex);
        throw ex;
      }
    });
  }

  private void seedAirport() {
    if ((Long) entityManager.createQuery("SELECT COUNT(a) FROM AirportEntity a").getSingleResult() == 0) {
      InitialData.airports.forEach(entityManager::persist);
    }
  }

  private void seedAircraft() {
    if ((Long) entityManager.createQuery("SELECT COUNT(a) FROM AircraftEntity a").getSingleResult() == 0) {
      InitialData.aircrafts.forEach(entityManager::persist);
    }
  }

  private void seedFlight() {
    if ((Long) entityManager.createQuery("SELECT COUNT(f) FROM FlightEntity f").getSingleResult() == 0) {
      InitialData.flights.forEach(entityManager::persist);
    }
  }

  private void seedSeat() {
    if ((Long) entityManager.createQuery("SELECT COUNT(s) FROM SeatEntity s").getSingleResult() == 0) {
      InitialData.seats.forEach(entityManager::persist);
    }
  }
}
