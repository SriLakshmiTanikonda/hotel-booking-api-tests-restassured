INSERT INTO guest_bookings (id, first_name, last_name, total_price, deposit_paid, check_in, check_out, additional_needs, active) VALUES
    (1, 'Ava',    'Patel',    450,  TRUE,  DATE '2027-01-10', DATE '2027-01-14', 'Breakfast',      TRUE),
    (2, 'Liam',   'Nguyen',   1200, FALSE, DATE '2027-02-01', DATE '2027-02-08', 'Late checkout',  TRUE),
    (3, 'Maya',   'Johnson',  99,   TRUE,  DATE '2027-03-15', DATE '2027-03-16', NULL,             TRUE),
    (4, 'Noah',   'Garcia',   780,  TRUE,  DATE '2027-04-20', DATE '2027-04-25', 'Airport pickup', TRUE),
    (5, 'Retired', 'Record',  10,   FALSE, DATE '2027-05-01', DATE '2027-05-02', NULL,             FALSE);
