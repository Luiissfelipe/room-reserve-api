CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    update_date TIMESTAMP NOT NULL
);

CREATE TABLE rooms (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    capacity INTEGER NOT NULL CHECK ( capacity > 0 ),
    active BOOLEAN NOT NULL,
    location VARCHAR(255) NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    update_date TIMESTAMP NOT NULL
);

CREATE TABLE reservations (
    id BIGSERIAL PRIMARY KEY,
    purpose VARCHAR(255) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    reservation_status VARCHAR(50) NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id),
    room_id BIGINT NOT NULL REFERENCES rooms(id),
    creation_date TIMESTAMP NOT NULL,
    update_date TIMESTAMP NOT NULL
);