-- KUNDEN
INSERT INTO customer (first_name, last_name, email, blocked)
VALUES ('Anna', 'Müller', 'anna.mueller@example.com', FALSE);       -- Kunde mit offenem Saldo

INSERT INTO customer (first_name, last_name, email, blocked)
VALUES ('Ben', 'Schmid', 'ben.schmid@example.com', FALSE);          -- Kunde mit Guthaben

INSERT INTO customer (first_name, last_name, email, blocked)
VALUES ('Clara', 'Koch', 'clara.koch@example.com', TRUE);           -- Gesperrter Kunde

-- ARTIKEL
INSERT INTO article (article_name, price) VALUES ('T-Shirt', 29.90);
INSERT INTO article (article_name, price) VALUES ('Jeans', 89.90);
INSERT INTO article (article_name, price) VALUES ('Sneakers', 119.90);

-- BESTELLUNGEN
-- Anna: normale Bestellung (offen, nicht bezahlt)
INSERT INTO orders (order_status, order_date, customer_id)
VALUES ('PLACED', NOW(), 1);

-- Ben: Bestellung älter als 3 Monate
INSERT INTO orders (order_status, order_date, customer_id)
VALUES ('PLACED', NOW() - INTERVAL '4 months', 2);

-- Clara: stornierte Bestellung
INSERT INTO orders (order_status, order_date, customer_id)
VALUES ('CANCELLED', NOW() - INTERVAL '5 months', 3);

-- Clara: Clara: Bestellung älter als 3 Monate + offener Saldo (Grund für Sperrung)
INSERT INTO orders (order_status, order_date, customer_id)
VALUES ('PLACED', NOW() - INTERVAL '4 months', 3);

-- BESTELLPOSITIONEN
INSERT INTO order_positions (quantity, price, order_id, article_id)
VALUES (2, 29.90, 1, 1);   -- Anna: 2x T-Shirt = 59.80

INSERT INTO order_positions (quantity, price, order_id, article_id)
VALUES (1, 89.90, 2, 2);   -- Ben: 1x Jeans = 89.90

INSERT INTO order_positions (quantity, price, order_id, article_id)
VALUES (1, 119.90, 4, 3);  -- Clara: 1x Sneakers = 119.90

-- ZAHLUNGEN
-- Anna zahlt zu wenig → offener Saldo
INSERT INTO payment (amount, customer_id)
VALUES (30.00, 1);   -- Anna zahlt 30.00, schuldet noch 29.80

-- Ben zahlt zu viel → Guthaben
INSERT INTO payment (amount, customer_id)
VALUES (120.00, 2);  -- Ben zahlt 120.00, Guthaben 30.10

