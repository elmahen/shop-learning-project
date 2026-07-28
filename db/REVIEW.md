# DB Folder Code Review

**Branch:** `feature/iteration-1c`  
**Date:** 2026-07-16  
**Scope:** `./db/` — schema, queries, testdata, shell scripts

---

## Findings

### 1. Q3/Q4 wrong JOIN type — worst debtors are invisible
**File:** `queries.sql:53` | **Severity:** Critical

Queries 3 and 4 use an INNER JOIN between the `Zahlungen` and `Bestellungen` CTEs. A customer who has placed orders but never made a payment has no row in `Zahlungen`, so the INNER JOIN drops them entirely — they never appear in the "negative saldo" list even though they owe the full order amount. Query 1 correctly handles this with `LEFT JOIN` + `COALESCE`.

**Fix:** Rewrite to start from `Bestellungen LEFT JOIN Zahlungen` with `COALESCE(z.total_paid, 0)`:
```sql
FROM Bestellungen b
LEFT JOIN Zahlungen z ON z.customer_id = b.customer_id
HAVING COALESCE(z.total_paid, 0) - b.total_ordered < 0
```

---

### 2. Q6 missing saldo check despite comment promising it
**File:** `queries.sql:85` | **Severity:** Critical

The comment states: *"offener Saldo + letzte Bestellung > 3 Monate"*, but the SQL only checks `c.blocked = TRUE` and `HAVING MAX(o.order_date) < NOW() - INTERVAL '3 months'`. No saldo is ever computed. A blocked customer with a zero or positive balance and an old order is incorrectly returned.

**Fix:** Add the `Zahlungen`/`Bestellungen` CTEs and a saldo condition to `HAVING`:
```sql
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
```

---

### 3. schema.sql is not idempotent
**File:** `schema.sql:1` | **Severity:** Medium

All five `CREATE TABLE` statements lack `IF NOT EXISTS`. Running `./schema.sh` a second time immediately errors with `relation already exists` for every table, leaving the schema in an undefined partial state. `reset.sh` avoids this via `DROP SCHEMA CASCADE`, but `schema.sh` has no equivalent guard.

**Fix:** Add `IF NOT EXISTS` to all `CREATE TABLE` statements, e.g.:
```sql
CREATE TABLE IF NOT EXISTS customer ( ... );
```

---

### 4. No `CHECK (amount > 0)` on `payment.amount`
**File:** `schema.sql:47` | **Severity:** Medium

`amount NUMERIC(10,2) NOT NULL` allows negative or zero values. The saldo queries sum `amount` directly, so a single negative-amount insert silently corrupts every balance report without any error.

**Fix:**
```sql
amount NUMERIC(10,2) NOT NULL CHECK (amount > 0),
```

---

### 5. reset.sh has no container-running guard
**File:** `reset.sh:12` | **Severity:** Medium

If `shop_db` is stopped, all three `docker exec` calls fail silently (non-zero exit, but the script continues). The final `echo "✓ Reset abgeschlossen"` still prints, giving a false success signal while the database is in the dropped state.

**Fix:** Add a guard before the first `docker exec`:
```bash
docker ps --filter "name=shop_db" --filter "status=running" | grep shop_db \
  || { echo "Error: shop_db container is not running"; exit 1; }
```

---

### 6. No `CHECK (quantity > 0)` on `order_positions.quantity`
**File:** `schema.sql:34` | **Severity:** Low

A zero or negative quantity is accepted silently. `quantity=0` creates a phantom line item; `quantity=-1` reduces the order total, creating an accidental refund path that distorts Q9's totals.

**Fix:**
```sql
quantity BIGINT NOT NULL CHECK (quantity > 0),
```

---

### 7. Redundant `GROUP BY` in Q3/Q4
**File:** `queries.sql:53` | **Severity:** Low (cleanup)

Both CTEs already emit exactly one row per `customer_id`, so the outer `GROUP BY z.customer_id, b.total_ordered, z.total_paid` is a no-op. It misleads readers into thinking re-aggregation is necessary, and the `HAVING` expression duplicates arithmetic already in the `SELECT` column. Should be a plain `WHERE` after removing the redundant grouping.

---

### 8. Duplicated CTEs and near-identical queries
**File:** `queries.sql:33, 53, 72` | **Severity:** Low (cleanup)

- The `Zahlungen`/`Bestellungen` CTE pair is copy-pasted verbatim in Q1, Q3, and Q4. A schema change to `payment.amount` or `order_positions.price` requires updates in three places.
- Q7 is Q2 plus `ORDER BY o.order_date ASC` — the distinction is unexplained and both hardcode `customer_id = 1`.

---

## Summary

| # | File | Severity | Finding |
|---|------|----------|---------|
| 1 | `queries.sql:53` | Critical | Q3/Q4 INNER JOIN drops customers with orders but no payments |
| 2 | `queries.sql:85` | Critical | Q6 saldo condition described in comment is never implemented |
| 3 | `schema.sql:1` | Medium | `CREATE TABLE` without `IF NOT EXISTS` — schema.sh not idempotent |
| 4 | `schema.sql:47` | Medium | `payment.amount` missing `CHECK (amount > 0)` |
| 5 | `reset.sh:12` | Medium | No guard for container not running — false success on failure |
| 6 | `schema.sql:34` | Low | `order_positions.quantity` missing `CHECK (quantity > 0)` |
| 7 | `queries.sql:53` | Low | Redundant `GROUP BY` in Q3/Q4 after CTEs already aggregate |
| 8 | `queries.sql:33` | Low | Duplicated CTEs across Q1/Q3/Q4 and near-identical Q2/Q7 |

**Two critical correctness bugs** (findings 1 and 2) produce wrong query results today with the current testdata and should be fixed before any business use. The medium findings are operational footguns. The low findings are cleanup.
