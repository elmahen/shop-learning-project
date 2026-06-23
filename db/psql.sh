#!/bin/bash
# ─────────────────────────────────────────
# psql.sh – Interaktive psql Session öffnen
# Voraussetzung: shop_db Container läuft
# Usage: ./db/psql.sh
# ─────────────────────────────────────────

docker exec -it shop_db psql -U shop_user -d shopdb
