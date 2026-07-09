-- ─────────────────────────────────────────────────────────────
-- queries.sql – Geschäftsfall-Queries (Iteration 1e)
-- Ausführen via: ./db/psql.sh → \i db/queries.sql
-- ─────────────────────────────────────────────────────────────

-- ── Ausweis-Queries ──────────────────────────────────────────

-- 1. Alle Kunden mit aktuellem Saldo


-- 2. Alle offenen Bestellungen eines Kunden mit Positionen und Gesamtbetrag


-- 3. Alle Kunden mit negativem Saldo (Zahlungen noch offen)


-- 4. Alle Kunden mit positivem Saldo (Guthaben vorhanden)


-- 5. Zahlungshistorie eines Kunden (chronologisch)


-- ── Geschäftslogik-Queries ───────────────────────────────────

-- 6. Alle gesperrten Kunden (offener Saldo + letzte Bestellung > 3 Monate)


-- 7. Offene Bestellungen eines Kunden in FIFO-Reihenfolge


-- 8. Alle stornierten Bestellungen


-- 9. Alle Bestellungen mit Status und Gesamtbetrag pro Bestellung

