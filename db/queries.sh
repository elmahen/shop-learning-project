#!/bin/bash
# ─────────────────────────────────────────
# queries.sh – Business case queries ausführen
# Usage: ./queries.sh
# ─────────────────────────────────────────

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

docker exec -i shop_db psql -U shop_user -d shopdb < "$SCRIPT_DIR/queries.sql"