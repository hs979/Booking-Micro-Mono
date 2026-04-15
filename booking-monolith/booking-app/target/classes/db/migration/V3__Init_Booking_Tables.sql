CREATE TABLE bookings
(
    id                   UUID    NOT NULL,
    created_at           TIMESTAMP WITHOUT TIME ZONE,
    created_by           BIGINT,
    last_modified        TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by     BIGINT,
    version              BIGINT,
    is_deleted           BOOLEAN NOT NULL,
    name                 VARCHAR(255),
    flight_number        VARCHAR(255),
    aircraft_id          UUID,
    departure_airport_id UUID,
    arrive_airport_id    UUID,
    flight_date          TIMESTAMP WITHOUT TIME ZONE,
    price                DECIMAL,
    description          VARCHAR(255),
    seat_number          VARCHAR(255),
    CONSTRAINT pk_bookings PRIMARY KEY (id)
);
