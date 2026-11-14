-- Test data for booking scenarios. In a real project this table would sit in
-- a shared test database; here an in-memory H2 database keeps the repo self-contained.
CREATE TABLE guest_bookings (
    id              INT PRIMARY KEY,
    first_name      VARCHAR(50)  NOT NULL,
    last_name       VARCHAR(50)  NOT NULL,
    total_price     INT          NOT NULL,
    deposit_paid    BOOLEAN      NOT NULL,
    check_in        DATE         NOT NULL,
    check_out       DATE         NOT NULL,
    additional_needs VARCHAR(100),
    active          BOOLEAN      DEFAULT TRUE NOT NULL,
    CONSTRAINT valid_stay CHECK (check_out > check_in)
);
