CREATE TABLE aircrafts
(
  id                 UUID    NOT NULL,
  created_at         TIMESTAMP WITHOUT TIME ZONE,
  created_by         BIGINT,
  last_modified      TIMESTAMP WITHOUT TIME ZONE,
  last_modified_by   BIGINT,
  version            BIGINT,
  is_deleted         BOOLEAN NOT NULL,
  name               VARCHAR(255),
  model              VARCHAR(255),
  manufacturing_year INTEGER NOT NULL,
  CONSTRAINT pk_aircrafts PRIMARY KEY (id)
);

CREATE TABLE airports
(
  id               UUID    NOT NULL,
  created_at       TIMESTAMP WITHOUT TIME ZONE,
  created_by       BIGINT,
  last_modified    TIMESTAMP WITHOUT TIME ZONE,
  last_modified_by BIGINT,
  version          BIGINT,
  is_deleted       BOOLEAN NOT NULL,
  name             VARCHAR(255),
  code             VARCHAR(255),
  address          VARCHAR(255),
  CONSTRAINT pk_airports PRIMARY KEY (id)
);

CREATE TABLE flights
(
  id                   UUID    NOT NULL,
  created_at           TIMESTAMP WITHOUT TIME ZONE,
  created_by           BIGINT,
  last_modified        TIMESTAMP WITHOUT TIME ZONE,
  last_modified_by     BIGINT,
  version              BIGINT,
  is_deleted           BOOLEAN NOT NULL,
  status               VARCHAR(255),
  flight_number        VARCHAR(255),
  departure_date       TIMESTAMP WITHOUT TIME ZONE,
  arrive_date          TIMESTAMP WITHOUT TIME ZONE,
  duration_minutes     DECIMAL,
  flight_date          TIMESTAMP WITHOUT TIME ZONE,
  price                DECIMAL,
  aircraft_id          UUID,
  departure_airport_id UUID,
  arrive_airport_id    UUID,
  CONSTRAINT pk_flights PRIMARY KEY (id)
);

CREATE TABLE seats
(
  id               UUID    NOT NULL,
  created_at       TIMESTAMP WITHOUT TIME ZONE,
  created_by       BIGINT,
  last_modified    TIMESTAMP WITHOUT TIME ZONE,
  last_modified_by BIGINT,
  version          BIGINT,
  is_deleted       BOOLEAN NOT NULL,
  type             VARCHAR(255),
  seat_class       VARCHAR(255),
  seat_number      VARCHAR(255),
  flight_id        UUID,
  CONSTRAINT pk_seats PRIMARY KEY (id)
);
