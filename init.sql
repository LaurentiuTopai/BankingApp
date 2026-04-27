-- Creare tabele (Hibernate le poate crea, dar e bine sa fie aici pentru siguranta containerului)
CREATE TABLE IF NOT EXISTS account (
    id SERIAL PRIMARY KEY,
    iban VARCHAR(255) NOT NULL UNIQUE,
    amount DECIMAL(19, 2) NOT NULL,
    name VARCHAR(255) NOT NULL,
    age INTEGER NOT NULL,
    time TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    account_id INTEGER REFERENCES account(id)
);

-- Inserare conturi de test din ptTestare.txt
INSERT INTO account (iban, amount, name, age, time) 
VALUES ('RO123BANC', 1000.00, 'Laurentiu', 25, NOW())
ON CONFLICT (iban) DO NOTHING;

INSERT INTO account (iban, amount, name, age, time) 
VALUES ('RO456BANC', 500.00, 'Test User', 30, NOW())
ON CONFLICT (iban) DO NOTHING;

-- Inserare utilizator de test (user: admin, parola: parola123)
-- Parola este hash-uita cu BCrypt
INSERT INTO users (username, password, account_id) 
VALUES ('admin', '$2a$10$8.UnVuG9HHgffUDAlk8q6OuVGkqCYAdVqK6vL.t.Z3y.uR.K3L7Ke', (SELECT id FROM account WHERE iban = 'RO123BANC'))
ON CONFLICT (username) DO NOTHING;
