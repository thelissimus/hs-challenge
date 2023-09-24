CREATE TYPE sex_t AS ENUM('male', 'female');

CREATE TABLE patients (
    first_name  VARCHAR(255) NOT NULL,
    middle_name VARCHAR(255) NOT NULL,
    last_name   VARCHAR(255) NOT NULL,
    sex         sex_t        NOT NULL,
    birth_date  DATE         NOT NULL,
    address     VARCHAR      NOT NULL,
    insurance   VARCHAR      NOT NULL,

    id          INTEGER GENERATED ALWAYS AS IDENTITY,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP,
    deleted_at  TIMESTAMP,

    PRIMARY KEY (id)
);
