package buildingblocks.contracts.flight;

import buildingblocks.core.event.IntegrationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FlightUpdated(
  UUID id,
  String flightNumber,
  UUID aircraftId,
  UUID departureAirportId,
  UUID arriveAirportId,
  LocalDateTime flightDate,
  BigDecimal price
) implements IntegrationEvent {
}
