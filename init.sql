
\c BancarDB;

INSERT INTO account (name, iban, amount, age, time) 
VALUES ('Laurentiu Sursa', 'RO123BANC', 1000.0, 25, NOW())
ON CONFLICT (iban) DO NOTHING;

INSERT INTO account (name, iban, amount, age, time) 
VALUES ('Destinatar Test', 'RO456BANC', 100.0, 30, NOW())
ON CONFLICT (iban) DO NOTHING;