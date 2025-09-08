SET search_path TO ms_applications;

INSERT INTO ms_applications.statuses (status_id, name, description)
VALUES (gen_random_uuid(), 'PENDING', 'Pending review by advisor'),
       (gen_random_uuid(), 'MANUAL_REVIEW', 'Manual review required'),
       (gen_random_uuid(), 'APPROVED', 'Approved'),
       (gen_random_uuid(), 'REJECTED', 'Rejected');


INSERT INTO ms_applications.loan_types
    (code, name, min_amount, max_amount, min_term, max_term, interest_rate, automatic_validation)
VALUES ('CONSUMER',  'Consumo',   500000.00,   30000000.00,   6,   84,  28.50, TRUE),
       ('BUSINESS',  'Negocios',  5000000.00,  90000000.00,  12,  120,  18.95, TRUE),
       ('MORTGAGE',  'Vivienda',  30000000.00, 900000000.00, 60,  360,  12.20, FALSE);