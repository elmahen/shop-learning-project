-- ─────────────────────────────────────────────────────────────
-- queries.sql – Geschäftsfall-Queries (Iteration 1e)
-- Ausführen via: ./db/psql.sh → \i db/queries.sql
-- ─────────────────────────────────────────────────────────────

-- ── Ausweis-Queries ──────────────────────────────────────────

-- Beispiel: Alle Kunden

select * from customer;

-- 1. Alle Kunden mit aktuellem Saldo

WITH Zahlungen AS (
    SELECT customer_id, SUM(amount) AS total_paid
    FROM payment
    GROUP BY customer_id
),
Bestellungen AS (
    SELECT o.customer_id, SUM(op.price * op.quantity) AS total_ordered
    FROM order_positions op
    JOIN orders o ON op.order_id = o.id
    GROUP BY o.customer_id
)
SELECT c.id, c.first_name, c.last_name,
       COALESCE(z.total_paid, 0) - COALESCE(b.total_ordered, 0) AS saldo
FROM customer c
LEFT JOIN Zahlungen z ON c.id = z.customer_id
LEFT JOIN Bestellungen b ON c.id = b.customer_id;

-- 2. Alle offenen Bestellungen eines Kunden mit Positionen und Gesamtbetrag

SELECT o.id, o.order_date, SUM(op.price * op.quantity) AS total
FROM orders o
JOIN order_positions op ON op.order_id = o.id
WHERE o.order_status = 'PLACED' AND o.customer_id = 1
GROUP BY o.id, o.order_date;

-- 3. Alle Kunden mit negativem Saldo (Zahlungen noch offen)

WITH Zahlungen AS (
    SELECT customer_id, SUM(amount) AS total_paid 
    FROM payment
    GROUP BY customer_id
    ),
Bestellungen AS (
    SELECT o.customer_id, SUM(op.price * op.quantity) AS total_ordered
    FROM order_positions op
    JOIN orders o ON op.order_id = o.id
    GROUP BY o.customer_id
    )
SELECT b.customer_id, COALESCE(z.total_paid, 0)  - b.total_ordered AS saldo 
FROM Bestellungen b LEFT JOIN Zahlungen z ON z.customer_id = b.customer_id 
GROUP BY b.customer_id, b.total_ordered, z.total_paid
HAVING COALESCE(z.total_paid, 0) - b.total_ordered < 0;

-- 4. Alle Kunden mit positivem Saldo (Guthaben vorhand4
    SELECT customer_id, SUM(amount) AS total_paid
    FROM payment
    GROUP BY customer_id
),
Bestellungen AS (
    SELECT o.customer_id, SUM(op.price * op.quantity) AS total_ordered
    FROM order_positions op
    JOIN orders o ON op.order_id = o.id
    GROUP BY o.customer_id
)
SELECT b.customer_id, COALESCE(z.total_paid, 0) - b.total_ordered AS saldo
FROM Bestellungen b LEFT JOIN Zahlungen z ON z.customer_id = b.customer_id
GROUP BY b.customer_id, b.total_ordered, z.total_paid
HAVING COALESCE(z.total_paid, 0) - b.total_ordered > 0;


-- 5. Zahlungshistorie eines Kunden (chronologisch)

SELECT * FROM payment WHERE customer_id = 1 ORDER BY date;

-- ── Geschäftslogik-Queries ───────────────────────────────────

-- 6. Alle gesperrten Kunden (offener Saldo + letzte Bestellung > 3 Monate)

WITH Zahlungen AS (
    SELECT customer_id, SUM(amount) AS total_paid FROM payment GROUP BY customer_id
),
Bestellungen AS (
    SELECT o.customer_id, SUM(op.price * op.quantity) AS total_ordered
    FROM order_positions op JOIN orders o ON op.order_id = o.id
    GROUP BY o.customer_id
)
SELECT c.*
FROM customer c
JOIN orders o ON o.customer_id = c.id
LEFT JOIN Zahlungen z ON z.customer_id = c.id
LEFT JOIN Bestellungen b ON b.customer_id = c.id
WHERE c.blocked = TRUE
GROUP BY c.id, z.total_paid, b.total_ordered
HAVING MAX(o.order_date) < NOW() - INTERVAL '3 months'
   AND COALESCE(z.total_paid, 0) - COALESCE(b.total_ordered, 0) < 0;

-- 7. Offene Bestellungen eines Kunden in FIFO-Reihenfolge

SELECT o.id, o.order_date, SUM(op.price * op.quantity) AS total
FROM orders o
JOIN order_positions op ON op.order_id = o.id
WHERE o.order_status = 'PLACED' AND o.customer_id = 1
GROUP BY o.id, o.order_date
ORDER BY o.order_date ASC;


-- 8. Alle stornierten Bestellungen

SELECT * FROM orders WHERE order_status = 'CANCELLED';


-- 9. Alle Bestellungen mit Status und Gesamtbetrag pro Bestellung

SELECT o.id, o.order_date, o.order_status, SUM(op.price * op.quantity) AS total
FROM orders o
LEFT JOIN order_positions op ON op.order_id = o.id
GROUP BY o.id, o.order_date, o.order_status
ORDER BY o.order_date ASC;

