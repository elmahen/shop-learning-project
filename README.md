# Shop – Lernprojekt Applikationsentwicklung

Dieses Repository ist das Starter-Template für das Sommerprojekt.  
Die vollständige Aufgabenstellung liegt unter [`docs/aufgabenstellung.md`](docs/aufgabenstellung.md).

---

## Tech Stack

| Schicht | Technologie |
|---------|-------------|
| Datenbank | PostgreSQL 16 (via Docker) |
| Backend | Java 21 · Spring Boot 3 · Spring Data JDBC |
| Frontend | React (Vite) |
| Build | Maven |
| REST Testing | Bruno |
| UML | PlantUML (VS Code Extension) |

---

## Voraussetzungen

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Java 21](https://adoptium.net/)
- [Node.js 20+](https://nodejs.org/) (via [nvm](https://github.com/nvm-sh/nvm))
- [VS Code](https://code.visualstudio.com/) mit empfohlenen Extensions (siehe `.vscode/extensions.json`)
- [Bruno](https://www.usebruno.com/)

---

## Projekt starten

### Datenbank

```bash
docker compose up -d
```

PostgreSQL läuft dann auf `localhost:5432`.  
Verbindung mit `psql`:

```bash
psql -h localhost -p 5432 -U shop_user -d shopdb
```

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

---

## Projektstruktur

```
shop-project/
├── docs/                          # Dokumentation & UML Diagramme
│   ├── aufgabenstellung.md        # Vollständige Aufgabenstellung
│   └── plantuml_referenz_bibliothek.puml  # UML Referenzbeispiel
├── db/                            # SQL Scripts
│   ├── schema.sql                 # Tabellen & Constraints
│   └── testdata.sql               # Testdaten
├── backend/                       # Spring Boot Applikation
│   └── src/main/java/com/example/shop/
│       ├── domain/                # Entitätsklassen
│       ├── repository/            # Spring Data JDBC Repositories
│       ├── service/               # Business-Logik & @Transactional
│       ├── rest/
│       │   ├── controller/        # REST Controller
│       │   └── dto/               # Request / Response DTOs
│       ├── scheduler/             # @Scheduled Jobs
│       └── exception/             # Exception-Klassen & @ControllerAdvice
├── frontend/                      # React Applikation (Vite)
├── bruno/                         # API Testscripts
├── docker-compose.yml
└── .vscode/                       # VS Code Einstellungen & Extensions
```

---

## Git Workflow

### Branch-Konvention

Pro Iteration wird ein eigener Feature Branch erstellt:

```
iteration/01-pure-sql
iteration/02-data-access
iteration/03-service-layer
iteration/04-rest-api
iteration/05-react-ui
```

### Ablauf pro Iteration

```bash
# 1. Neuen Branch erstellen
git checkout -b iteration/01-pure-sql

# 2. Arbeiten, committen
git add .
git commit -m "feat: add customer table with constraints"

# 3. Branch pushen
git push -u origin iteration/01-pure-sql

# 4. Pull Request auf GitHub öffnen → Code Review
# 5. Nach Abnahme: Merge in main
```

### Commit Message Konvention

```
feat:  neues Feature
fix:   Bugfix
docs:  Dokumentation
test:  Tests
refactor: Refactoring ohne Funktionsänderung
```

Beispiele:
```
feat: add order table with status enum
fix: correct foreign key constraint on order_items
docs: add UML class diagram iteration 1
test: add repository integration tests for customer
```

---

## UML Diagramme

PlantUML Diagramme liegen im `docs/` Ordner als `.puml` Files.  
Mit der VS Code Extension `jebbs.plantuml` kann eine Live-Vorschau geöffnet werden:

```
Alt+D  →  Diagramm-Vorschau öffnen
```

Ein Referenzbeispiel mit allen wichtigen UML-Konstrukten liegt unter:  
[`docs/plantuml_referenz_bibliothek.puml`](docs/plantuml_referenz_bibliothek.puml)

---

## Iteration Übersicht

| Iteration | Branch | Inhalt | Status |
|-----------|--------|--------|--------|
| 1 | `iteration/01-pure-sql` | UML · PostgreSQL · Schema · Testdaten | ⬜ |
| 2 | `iteration/02-data-access` | Spring Boot · Spring Data JDBC · Repositories · Tests | ⬜ |
| 3 | `iteration/03-service-layer` | Services · Zahlungslogik · @Transactional · Scheduler | ⬜ |
| 4 | `iteration/04-rest-api` | REST Controller · DTOs · Fehlerbehandlung · Bruno Tests | ⬜ |
| 5 | `iteration/05-react-ui` | React · Komponenten · API Integration | ⬜ |

---

*Viel Erfolg! 🚀*
