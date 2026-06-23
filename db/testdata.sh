#!/bin/bash
# ─────────────────────────────────────────
# testdata.sh – Testdaten laden
# Voraussetzung: Schema ist bereits geladen
# Usage: ./db/testdata.sh
# ─────────────────────────────────────────

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "→ Testdaten laden..."
docker exec -i shop_db psql -U shop_user -d shopdb < "$SCRIPT_DIR/testdata.sql"
echo "✓ Testdaten geladen"
