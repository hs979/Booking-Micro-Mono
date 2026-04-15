CREATE TABLE passengers
(
    id               UUID    NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE,
    created_by       BIGINT,
    last_modified    TIMESTAMP WITHOUT TIME ZONE,
    last_modified_by BIGINT,
    version          BIGINT,
    is_deleted       BOOLEAN NOT NULL,
    passenger_type   VARCHAR(255),
    name             VARCHAR(255),
    passport_number  VARCHAR(255),
    age              INTEGER NOT NULL,
    CONSTRAINT pk_passengers PRIMARY KEY (id)
);
