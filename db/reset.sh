#!/bin/bash
# ─────────────────────────────────────────
# reset.sh – DB komplett zurücksetzen
# Drop → Schema → Testdaten
# Voraussetzung: shop_db Container läuft
# Usage: ./db/reset.sh
# ─────────────────────────────────────────

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "→ Alle Tabellen droppen..."
docker exec -i shop_db psql -U shop_user -d shopdb << 'EOF'
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO shop_user;
EOF

echo "→ Schema laden..."
docker exec -i shop_db psql -U shop_user -d shopdb < "$SCRIPT_DIR/schema.sql"

echo "→ Testdaten laden..."
docker exec -i shop_db psql -U shop_user -d shopdb < "$SCRIPT_DIR/testdata.sql"

echo "✓ Reset abgeschlossen"
