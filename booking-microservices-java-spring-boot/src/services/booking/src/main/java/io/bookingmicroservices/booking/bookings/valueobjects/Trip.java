package io.bookingmicroservices.booking.bookings.valueobjects;

import buildingblocks.utils.validation.ValidationUtils;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Embeddable
@EqualsAndHashCode
@NoArgsConstructor // Required by JPA
@Getter
public class Trip {
    private UUID flightId;
    private String flightNumber;
    private UUID aircraftId;
    private UUID departureAirportId;
    private UUID arriveAirportId;
    private LocalDateTime flightDate;
    private BigDecimal price;
    private String description;
    private String seatNumber;

    public Trip(
            UUID flightId,
            String flightNumber,
            UUID aircraftId,
            UUID departureAirportId,
            UUID arriveAirportId,
            LocalDateTime flightDate,
            BigDecimal price,
            String description,
            String seatNumber) {

        ValidationUtils.notBeNullOrEmpty(flightId);
        ValidationUtils.notBeNullOrEmpty(flightNumber);
        ValidationUtils.notBeNullOrEmpty(aircraftId);
        ValidationUtils.notBeNullOrEmpty(departureAirportId);
        ValidationUtils.notBeNullOrEmpty(arriveAirportId);
        ValidationUtils.validLocalDateTime(flightDate);
        ValidationUtils.notBeNegativeOrNull(price);
        ValidationUtils.notBeNullOrEmpty(description);
        ValidationUtils.notBeNullOrEmpty(seatNumber);

        this.flightId = flightId;
        this.flightNumber = flightNumber;
        this.aircraftId = aircraftId;
        this.departureAirportId = departureAirportId;
        this.arriveAirportId = arriveAirportId;
        this.flightDate = flightDate;
        this.price = price;
        this.description = description;
        this.seatNumber = seatNumber;
    }
}

