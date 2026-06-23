#!/bin/bash
# ─────────────────────────────────────────
# schema.sh – Datenbankschema laden
# Voraussetzung: shop_db Container läuft
# Usage: ./db/schema.sh
# ─────────────────────────────────────────

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "→ Schema laden..."
docker exec -i shop_db psql -U shop_user -d shopdb < "$SCRIPT_DIR/schema.sql"
echo "✓ Schema geladen"
