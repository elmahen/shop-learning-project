#!/bin/bash
# ─────────────────────────────────────────
# reset.sh – DB komplett zurücksetzen
# Drop → Schema → Testdaten
# Voraussetzung: shop_db Container läuft
# Usage: ./reset.sh
# ─────────────────────────────────────────

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

docker ps --filter "name=shop_db" --filter "status=running" | grep shop_db \
  || { echo "Error: shop_db container is not running"; exit 1; }

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
